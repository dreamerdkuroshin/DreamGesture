use std::sync::Arc;
use tokio::sync::RwLock;

#[derive(Clone)]
pub struct AppState {
    pub discovery_running: Arc<RwLock<bool>>,
    pub gesture_running: Arc<RwLock<bool>>,
    pub transfer_state: Arc<RwLock<String>>,
}

impl AppState {
    pub fn new() -> Self {
        AppState {
            discovery_running: Arc::new(RwLock::new(false)),
            gesture_running: Arc::new(RwLock::new(false)),
            transfer_state: Arc::new(RwLock::new("idle".to_string())),
        }
    }
}
