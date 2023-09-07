use std::{sync::mpsc::channel, thread, time::{Instant, Duration}, collections::HashMap, cmp::{min, max}};

use clap::Parser;

use handler::{run_cached, run_uncached};
use postgres::{Client, NoTls};
use rand::Rng;

use crate::requests::{random_select, random_update_or_delete};

mod handler;
mod requests;

/// DB cacher
#[derive(Parser, Debug, Clone)]
#[clap(author, version, about, long_about = None)]
struct Args {
    /// Cache Capacity
    #[clap(short, long, default_value_t = 16)]
    cache_cap: usize,

    /// Controls Select/(Update & Delete) ratio - e.g. 0.95 means 95% of requests would be Select
    #[clap(short, long, default_value_t = 0.95)]
    select_chance: f32,
    
    /// Number of Iterations for testbench
    #[clap(short, long, default_value_t = 1000)]
    n_requests: i32,
}

struct RequestResults {
    min_time: Duration,
    max_time: Duration,
    total_time: Duration,
    n: u32,
}

type Results = HashMap<String, RequestResults>;

fn main() {
    let args = Args::parse();
    test_cached(args.clone());
    test_uncached(args.clone());
}

fn test_cached(args: Args) {
    
    let results = test(true, args.clone());

    println!();
    println!("Results (Cached) with: cache_cap = {}, select_chance = {}, n_requests = {}", args.cache_cap, args.select_chance, args.n_requests);
    println!();
    
    print_results(results);
}

fn test_uncached(args: Args) {

    let results = test(false, args.clone());

    println!();
    println!("Results (Uncached) with: select_chance = {}, n_requests = {}", args.select_chance, args.n_requests);
    println!();

    print_results(results);
}

fn test(cached: bool, args: Args) -> Results {
    let client = Client::connect("host=localhost user=postgres dbname=railway", NoTls).unwrap();

    let (request_tx, request_rx) = channel();
    let (response_tx, response_rx) = channel();

    thread::spawn(move || {
        if cached {
            run_cached(client, request_rx, response_tx, args.cache_cap);
        } else {
            run_uncached(client, request_rx, response_tx);
        }
    });

    let mut rng = rand::thread_rng();
    let mut results: Results = HashMap::new();
    for i in 0..args.n_requests {
        let request = if rng.gen_range(0.0..1.0) > args.select_chance {
            random_update_or_delete()
        } else {
            random_select()
        };
        let request_name = request.name.clone();
        let start = Instant::now();
        request_tx.send(request);
        response_rx.recv();
        let duration = start.elapsed();
        if let Some(rr) = results.get_mut(request_name) {
            rr.min_time = min(rr.min_time, duration);
            rr.max_time = max(rr.max_time, duration);
            rr.total_time += duration;
            rr.n += 1;
        } else {
            results.insert(request_name.to_string(), RequestResults { min_time: duration, max_time: duration, total_time: duration, n: 1 });
        }
    }
    results
}

fn print_results(results: Results) {
    let mut keys: Vec<&String> = results.keys().collect();
    keys.sort();
    for key in keys {
        let v = results.get(key).unwrap();
        println!("{}:\n\tmin_time = {:?}\n\tmax_time = {:?}\n\tavg_time = {:?}", key, v.min_time, v.max_time, v.total_time / v.n);
    }
    println!();
    println!();
}