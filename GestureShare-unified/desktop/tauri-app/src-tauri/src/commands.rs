use serde::{Deserialize, Serialize};
use tauri::State;

use crate::state::AppState;

#[derive(Debug, Serialize)]
pub struct DeviceInfo {
    pub name: String,
    pub device_type: String,
    pub protocol_version: u16,
    pub platform: String,
}

#[tauri::command]
pub fn get_device_info() -> DeviceInfo {
    let platform = if cfg!(target_os = "windows") {
        "windows"
    } else if cfg!(target_os = "macos") {
        "macos"
    } else {
        "linux"
    };

    DeviceInfo {
        name: whoami::devicename(),
        device_type: "desktop".to_string(),
        protocol_version: 1,
        platform: platform.to_string(),
    }
}

#[tauri::command]
pub async fn start_discovery(state: State<'_, AppState>) -> Result<String, String> {
    let mut running = state.discovery_running.write().await;
    *running = true;
    Ok("Discovery started".to_string())
}

#[tauri::command]
pub async fn stop_discovery(state: State<'_, AppState>) -> Result<String, String> {
    let mut running = state.discovery_running.write().await;
    *running = false;
    Ok("Discovery stopped".to_string())
}

#[derive(Debug, Serialize, Deserialize)]
pub struct DiscoveredDeviceDto {
    pub device_id: String,
    pub name: String,
    pub device_type: String,
    pub address: String,
    pub port: u16,
    pub signal_strength: i32,
}

#[tauri::command]
pub async fn get_discovered_devices() -> Vec<DiscoveredDeviceDto> {
    vec![]
}

#[tauri::command]
pub async fn start_gesture_detection(state: State<'_, AppState>) -> Result<String, String> {
    let mut running = state.gesture_running.write().await;
    *running = true;
    Ok("Gesture detection started".to_string())
}

#[tauri::command]
pub async fn stop_gesture_detection(state: State<'_, AppState>) -> Result<String, String> {
    let mut running = state.gesture_running.write().await;
    *running = false;
    Ok("Gesture detection stopped".to_string())
}

#[derive(Debug, Deserialize)]
pub struct SendFileRequest {
    pub file_path: String,
    pub target_device_id: String,
}

#[tauri::command]
pub async fn send_file(request: SendFileRequest) -> Result<String, String> {
    tracing::info!("Sending file: {} to {}", request.file_path, request.target_device_id);
    Ok("Transfer initiated".to_string())
}

#[tauri::command]
pub async fn cancel_transfer() -> Result<String, String> {
    Ok("Transfer cancelled".to_string())
}

#[tauri::command]
pub async fn get_transfer_state(state: State<'_, AppState>) -> String {
    state.transfer_state.read().await.clone()
}

#[derive(Debug, Serialize)]
pub struct ScreenshotResult {
    pub path: String,
    pub width: u32,
    pub height: u32,
}

#[tauri::command]
pub async fn capture_screenshot() -> Result<ScreenshotResult, String> {
    Ok(ScreenshotResult {
        path: "/tmp/gesture_screenshot.png".to_string(),
        width: 1920,
        height: 1080,
    })
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AppSettings {
    pub auto_accept: bool,
    pub gesture_sensitivity: f32,
    pub discovery_port: u16,
    pub save_directory: String,
    pub enable_sounds: bool,
    pub enable_notifications: bool,
    pub dark_mode: bool,
}

impl Default for AppSettings {
    fn default() -> Self {
        AppSettings {
            auto_accept: false,
            gesture_sensitivity: 0.95,
            discovery_port: 57771,
            save_directory: dirs::download_dir()
                .unwrap_or_default()
                .to_string_lossy()
                .to_string(),
            enable_sounds: true,
            enable_notifications: true,
            dark_mode: true,
        }
    }
}

#[tauri::command]
pub fn get_settings() -> AppSettings {
    AppSettings::default()
}

#[tauri::command]
pub fn set_settings(settings: AppSettings) -> Result<String, String> {
    tracing::info!("Settings updated: {:?}", settings);
    Ok("Settings saved".to_string())
}
