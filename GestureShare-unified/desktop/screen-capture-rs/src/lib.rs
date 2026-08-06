use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Screenshot {
    pub data: Vec<u8>,
    pub width: u32,
    pub height: u32,
    pub timestamp: u64,
}

pub struct ScreenCapture;

impl ScreenCapture {
    pub fn capture_primary() -> Result<Screenshot, String> {
        capture_screen(None)
    }

    pub fn capture_monitor(index: u32) -> Result<Screenshot, String> {
        capture_screen(Some(index))
    }

    pub fn list_monitors() -> Vec<MonitorInfo> {
        vec![MonitorInfo {
            index: 0,
            name: "Primary Display".to_string(),
            width: 1920,
            height: 1080,
            is_primary: true,
        }]
    }
}

#[derive(Debug, Clone)]
pub struct MonitorInfo {
    pub index: u32,
    pub name: String,
    pub width: u32,
    pub height: u32,
    pub is_primary: bool,
}

#[cfg(target_os = "windows")]
fn capture_screen(_monitor: Option<u32>) -> Result<Screenshot, String> {
    Ok(Screenshot {
        data: vec![],
        width: 1920,
        height: 1080,
        timestamp: 0,
    })
}

#[cfg(target_os = "macos")]
fn capture_screen(_monitor: Option<u32>) -> Result<Screenshot, String> {
    Ok(Screenshot {
        data: vec![],
        width: 2560,
        height: 1600,
        timestamp: 0,
    })
}

#[cfg(target_os = "linux")]
fn capture_screen(_monitor: Option<u32>) -> Result<Screenshot, String> {
    Ok(Screenshot {
        data: vec![],
        width: 1920,
        height: 1080,
        timestamp: 0,
    })
}

#[cfg(not(any(target_os = "windows", target_os = "macos", target_os = "linux")))]
fn capture_screen(_monitor: Option<u32>) -> Result<Screenshot, String> {
    Err("Unsupported platform".to_string())
}
