#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod commands;
mod state;
mod gesture_service;

use tauri::{Manager, SystemTray, SystemTrayEvent, SystemTrayMenu, SystemTrayMenuItem};
use tracing::info;

fn main() {
    tracing_subscriber::fmt()
        .with_env_filter("info")
        .init();

    let tray_menu = SystemTrayMenu::new()
        .add_item(SystemTrayMenuItem::new("show", "Show GestureShare"))
        .add_native_item(SystemTrayMenuItem::Separator)
        .add_item(SystemTrayMenuItem::new("start", "Start Detection"))
        .add_item(SystemTrayMenuItem::new("stop", "Stop Detection"))
        .add_native_item(SystemTrayMenuItem::Separator)
        .add_item(SystemTrayMenuItem::new("quit", "Quit"));

    let system_tray = SystemTray::new().with_menu(tray_menu);

    tauri::Builder::default()
        .manage(state::AppState::new())
        .system_tray(system_tray)
        .on_system_tray_event(|app, event| match event {
            SystemTrayEvent::LeftClick { .. } => {
                if let Some(window) = app.get_window("main") {
                    let _ = window.show();
                    let _ = window.set_focus();
                }
            }
            SystemTrayEvent::MenuItemClick { id, .. } => match id.as_str() {
                "show" => {
                    if let Some(window) = app.get_window("main") {
                        let _ = window.show();
                        let _ = window.set_focus();
                    }
                }
                "start" => {
                    let _ = app.emit_all("tray-start-detection", ());
                }
                "stop" => {
                    let _ = app.emit_all("tray-stop-detection", ());
                }
                "quit" => {
                    std::process::exit(0);
                }
                _ => {}
            },
            _ => {}
        })
        .invoke_handler(tauri::generate_handler![
            commands::get_device_info,
            commands::start_discovery,
            commands::stop_discovery,
            commands::get_discovered_devices,
            commands::start_gesture_detection,
            commands::stop_gesture_detection,
            commands::send_file,
            commands::cancel_transfer,
            commands::get_transfer_state,
            commands::capture_screenshot,
            commands::get_settings,
            commands::set_settings,
        ])
        .setup(|app| {
            info!("GestureShare Desktop starting...");
            let app_handle = app.handle();
            tokio::spawn(async move {
                gesture_service::init_gesture_service(app_handle).await;
            });
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
