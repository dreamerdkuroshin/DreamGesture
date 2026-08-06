use tracing::{debug, info, warn};

pub struct WebcamCapture {
    device_index: u32,
    width: u32,
    height: u32,
    fps: u32,
}

impl WebcamCapture {
    pub fn new(device_index: u32, width: u32, height: u32, fps: u32) -> Self {
        WebcamCapture {
            device_index,
            width,
            height,
            fps,
        }
    }

    pub fn default_device() -> Self {
        Self::new(0, 1280, 720, 15)
    }

    pub fn list_devices() -> Vec<CameraInfo> {
        let mut devices = vec![];
        for i in 0..4 {
            devices.push(CameraInfo {
                index: i,
                name: format!("Camera {}", i),
            });
        }
        devices
    }

    pub fn start(&self) -> Result<(), String> {
        info!(
            "Starting webcam capture: device={}, {}x{}@{}fps",
            self.device_index, self.width, self.height, self.fps
        );
        Ok(())
    }

    pub fn stop(&self) {
        info!("Stopping webcam capture");
    }

    pub fn capture_frame(&self) -> Option<Vec<u8>> {
        let frame_size = (self.width * self.height * 4) as usize;
        let frame = vec![0u8; frame_size];
        Some(frame)
    }
}

#[derive(Debug, Clone)]
pub struct CameraInfo {
    pub index: u32,
    pub name: String,
}
