use std::vec;

use postgres::Row;
use rand::Rng;

pub enum Entity {
    Station,
    Route,
    RouteSection,
}

pub enum Statement {
    Select(String),
    Delete(String),
    Update(String),
}

pub struct Request {
    pub name: &'static str,
    pub tables: Vec<String>,
    pub statement: Statement,
    pub entity: Option<Entity>,
}

pub fn row_to_string(row: &Row, result_type: &Entity) -> String {
    match result_type {
        Entity::Station => {
            let id: i32 = row.get(0);
            let name: String = row.get(1);
            let latitude: f32 = row.get(2);
            let longitude: f32 = row.get(3);
            format!("{id} {name} {latitude} {longitude}")
        }
        Entity::Route => {
            let id: i32 = row.get(0);
            let name: String = row.get(1);
            let first_station_id: i32 = row.get(2);
            let last_station_id: i32 = row.get(3);
            format!("{id} {name} {first_station_id} {last_station_id}")
        }
        Entity::RouteSection => {
            let id: i32 = row.get(0);
            let route_id: i32 = row.get(1);
            let cost: f32 = row.get(4);
            let departure_station_id: i32 = row.get(5);
            let destination_station_id: i32 = row.get(6);
            format!("{id} {route_id} {departure_station_id} {destination_station_id} {cost}")
        }
    }
}

pub fn random_select() -> Request {
    let mut rng = rand::thread_rng();
    match rng.gen_range(0..5) {
        0 => select_all_routes(),
        1 => select_all_route_sections(),
        2 => select_routes_with_latitude_more_than(),
        3 => select_route_sections_with_cost_more_than(),
        _ => select_all_stations(),
    }
}

fn select_all_stations() -> Request {
    Request {
        name: "Select all stations",
        tables: vec!["stations".to_string()],
        statement: Statement::Select("SELECT * FROM stations".to_string()),
        entity: Some(Entity::Station),
    }
}

fn select_all_routes() -> Request {
    Request {
        name: "Select all routes",
        tables: vec!["routes".to_string()],
        statement: Statement::Select("SELECT * FROM routes".to_string()),
        entity: Some(Entity::Route),
    }
}

fn select_all_route_sections() -> Request {
    Request {
        name: "Select all route sections",
        tables: vec!["route_sections".to_string()],
        statement: Statement::Select("SELECT * FROM route_sections".to_string()),
        entity: Some(Entity::RouteSection),
    }
}

fn select_routes_with_latitude_more_than() -> Request {
    let mut rng = rand::thread_rng();
    let latitude = rng.gen_range(-8..=8) * 10;
    Request {
        name: "Select routes with latitude more than",
        tables: vec!["stations".to_string(), "routes".to_string()],
        statement: Statement::Select(format!("SELECT * FROM routes WHERE routes.first_station_id IN ( SELECT stations.id FROM stations WHERE latitude > {latitude} )")),
        entity: Some(Entity::Route),
    }
}

fn select_route_sections_with_cost_more_than() -> Request {
    let mut rng = rand::thread_rng();
    let cost: i32 = rng.gen_range(0..10) * 100;
    Request {
        name: "Select route sections with cost more than",
        tables: vec!["route_sections".to_string()],
        statement: Statement::Select(format!("SELECT * FROM route_sections WHERE route_sections.cost > {cost}")),
        entity: Some(Entity::RouteSection),
    }
}

pub fn random_update_or_delete() -> Request {
    let mut rng = rand::thread_rng();
    match rng.gen_range(0..3) {
        0 => update_route_sections_cost(),
        1 => update_stations_location(),
        _ => delete_unused_stations(),
    }
}

fn update_route_sections_cost() -> Request {
    Request {
        name: "Update route sections cost",
        tables: vec!["route_sections".to_string()],
        statement: Statement::Update("UPDATE route_sections SET cost = cost + 10".to_string()),
        entity: None,
    }
}

fn update_stations_location() -> Request {
    Request {
        name: "Update stations location",
        tables: vec!["stations".to_string()],
        statement: Statement::Update("UPDATE stations SET longitude = -longitude".to_string()),
        entity: None,
    }
}

fn delete_unused_stations() -> Request {
    Request {
        name: "Delete unused stations",
        tables: vec!["stations".to_string()],
        statement: Statement::Delete("
                DELETE FROM stations
                WHERE id NOT IN (
                SELECT (departure_station_id)
                FROM route_sections
                UNION
                SELECT (destination_station_id)
                FROM route_sections
                UNION
                SELECT (first_station_id)
                FROM routes
                UNION
                SELECT (last_station_id)
                FROM routes
            )".to_string()),
        entity: None,
    }
}