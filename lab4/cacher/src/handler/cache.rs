use lru::LruCache;
use std::collections::HashMap;

pub type Query = String;
pub type Table = String;
pub type Data = String;

pub struct QueryCache {
    storage: LruCache<Query, Data>,
    related_queries: HashMap<Table, Vec<Query>>,
}

impl QueryCache {
    pub fn new(capacity: usize) -> Self {
        QueryCache {
            storage: LruCache::new(capacity),
            related_queries: HashMap::new(),
        }
    }

    pub fn add_entry(&mut self, query: Query, data: Data, used_tables: Vec<Table>) {
        for t in used_tables {
            if let Some(v) = self.related_queries.get_mut(&t) {
                v.push(query.clone());
            } else {
                self.related_queries.insert(t, vec![query.clone()]);
            }
        }
        self.storage.push(query, data);
    }

    pub fn get_entry(&mut self, query: &Query) -> Option<&Data> {
        self.storage.get(query)
    }

    pub fn invalidate_table(&mut self, table: &Table) {
        if let Some(v) = self.related_queries.get_mut(table) {
            for q in v.iter() {
                self.storage.pop(q);
            }
            v.clear();
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_lru() {
        let mut query_cache = QueryCache::new(3);

        let (query_1, data_1) = ("q1".to_string(), "d1".to_string());
        let (query_2, data_2) = ("q2".to_string(), "d2".to_string());
        let (query_3, data_3) = ("q3".to_string(), "d3".to_string());
        let (query_4, data_4) = ("q4".to_string(), "d4".to_string());

        query_cache.add_entry(query_1.clone(), data_1.clone(), vec![]);
        query_cache.add_entry(query_2.clone(), data_2.clone(), vec![]);
        query_cache.add_entry(query_3.clone(), data_3.clone(), vec![]);
        assert_eq!(query_cache.get_entry(&query_2), Some(&data_2));
        assert_eq!(query_cache.get_entry(&query_1), Some(&data_1));
        assert_eq!(query_cache.get_entry(&query_3), Some(&data_3));

        query_cache.add_entry(query_4.clone(), data_4.clone(), vec![]);
        assert_eq!(query_cache.get_entry(&query_2), None);
        assert_eq!(query_cache.get_entry(&query_1), Some(&data_1));
        assert_eq!(query_cache.get_entry(&query_3), Some(&data_3));
        assert_eq!(query_cache.get_entry(&query_4), Some(&data_4));
    }

    #[test]
    fn test_invalidation() {
        let mut query_cache = QueryCache::new(4);

        let tables = vec![
            "t1".to_string(),
            "t2".to_string(),
            "t3".to_string(),
            "t4".to_string(),
        ];
        let (query_1, data_1) = ("q1".to_string(), "d1".to_string());
        let (query_2, data_2) = ("q2".to_string(), "d2".to_string());
        let (query_3, data_3) = ("q3".to_string(), "d3".to_string());
        let (query_4, data_4) = ("q4".to_string(), "d4".to_string());

        query_cache.add_entry(query_1.clone(), data_1.clone(), vec![tables[0].clone()]);
        query_cache.add_entry(query_2.clone(), data_2.clone(), vec![tables[1].clone()]);
        query_cache.add_entry(query_3.clone(), data_3.clone(), vec![tables[2].clone()]);
        query_cache.add_entry(query_4.clone(), data_4.clone(), vec![tables[3].clone(), tables[0].clone()],);
        assert_eq!(query_cache.get_entry(&query_1), Some(&data_1));
        assert_eq!(query_cache.get_entry(&query_2), Some(&data_2));
        assert_eq!(query_cache.get_entry(&query_3), Some(&data_3));
        assert_eq!(query_cache.get_entry(&query_4), Some(&data_4));

        query_cache.invalidate_table(&tables[1]);
        assert_eq!(query_cache.get_entry(&query_1), Some(&data_1));
        assert_eq!(query_cache.get_entry(&query_2), None);
        assert_eq!(query_cache.get_entry(&query_3), Some(&data_3));
        assert_eq!(query_cache.get_entry(&query_4), Some(&data_4));

        query_cache.invalidate_table(&tables[0]);
        assert_eq!(query_cache.get_entry(&query_1), None);
        assert_eq!(query_cache.get_entry(&query_2), None);
        assert_eq!(query_cache.get_entry(&query_3), Some(&data_3));
        assert_eq!(query_cache.get_entry(&query_4), None);

        query_cache.invalidate_table(&tables[3]);
        assert_eq!(query_cache.get_entry(&query_1), None);
        assert_eq!(query_cache.get_entry(&query_2), None);
        assert_eq!(query_cache.get_entry(&query_3), Some(&data_3));
        assert_eq!(query_cache.get_entry(&query_4), None);
    }
}
