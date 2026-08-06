use tauri::AppHandle;
use tracing::info;

pub async fn init_gesture_service(_app_handle: AppHandle) {
    info!("Initializing gesture recognition service...");
}
