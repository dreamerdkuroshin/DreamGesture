use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::{Arc, Mutex};
use std::time::{Duration, Instant};
use tokio::net::UdpSocket;
use tokio::sync::broadcast;
use tracing::{debug, info, warn};

use crate::protocol::{DiscoveryPayload, DeviceType};
use crate::{ProtocolError, Result, DEFAULT_PORT};

const DISCOVERY_MULTICAST_ADDR: &str = "239.255.42.99";
const DISCOVERY_INTERVAL_SECS: u64 = 3;
const DEVICE_TIMEOUT_SECS: u64 = 15;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DiscoveredDevice {
    pub device_id: String,
    pub name: String,
    pub device_type: DeviceType,
    pub address: SocketAddr,
    pub port: u16,
    pub capabilities: Vec<String>,
    pub last_seen: u64,
    pub signal_strength: i32,
    pub protocol_version: u16,
}

impl DiscoveredDevice {
    pub fn is_expired(&self) -> bool {
        let now = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();
        now - self.last_seen > DEVICE_TIMEOUT_SECS
    }
}

#[derive(Clone)]
pub struct DiscoveryService {
    device_id: String,
    device_name: String,
    device_type: DeviceType,
    port: u16,
    capabilities: Vec<String>,
    devices: Arc<Mutex<HashMap<String, DiscoveredDevice>>>,
    tx: broadcast::Sender<Vec<DiscoveredDevice>>,
}

impl DiscoveryService {
    pub fn new(
        device_name: String,
        device_type: DeviceType,
        port: u16,
        capabilities: Vec<String>,
    ) -> Self {
        let (tx, _) = broadcast::channel(16);
        DiscoveryService {
            device_id: uuid::Uuid::new_v4().to_string(),
            device_name,
            device_type,
            port,
            capabilities,
            devices: Arc::new(Mutex::new(HashMap::new())),
            tx,
        }
    }

    pub fn subscribe(&self) -> broadcast::Receiver<Vec<DiscoveredDevice>> {
        self.tx.subscribe()
    }

    pub async fn start(&self) -> Result<()> {
        let socket = UdpSocket::bind(format!("0.0.0.0:{}", self.port)).await?;
        socket.set_broadcast(true).map_err(|e| {
            ProtocolError::Discovery(format!("Failed to set broadcast: {}", e))
        })?;

        let multicast: std::net::SocketAddr =
            format!("{}:{}", DISCOVERY_MULTICAST_ADDR, self.port)
                .parse()
                .map_err(|e| ProtocolError::Discovery(format!("Invalid multicast addr: {}", e)))?;

        socket
            .connect(multicast)
            .await
            .map_err(|e| ProtocolError::Discovery(format!("Connect failed: {}", e)))?;

        let recv_socket = UdpSocket::bind(format!("0.0.0.0:{}", self.port)).await?;

        let broadcast_payload = DiscoveryPayload {
            device_id: self.device_id.clone(),
            device_name: self.device_name.clone(),
            device_type: self.device_type.clone(),
            capabilities: self.capabilities.clone(),
            port: self.port,
        };
        let broadcast_bytes = serde_json::to_vec(&broadcast_payload).unwrap_or_default();

        let self_clone = self.clone();
        tokio::spawn(async move {
            let mut interval = tokio::time::interval(Duration::from_secs(DISCOVERY_INTERVAL_SECS));
            loop {
                interval.tick().await;
                let _ = socket.send(&broadcast_bytes).await;
                debug!("Broadcast discovery packet");
                self_clone.cleanup_expired();
            }
        });

        let self_clone = self.clone();
        tokio::spawn(async move {
            let mut buf = vec![0u8; 4096];
            loop {
                match recv_socket.recv_from(&mut buf).await {
                    Ok((len, addr)) => {
                        if let Ok(payload) = serde_json::from_slice::<DiscoveryPayload>(&buf[..len]) {
                            if payload.device_id != self_clone.device_id {
                                self_clone.add_or_update_device(payload, addr);
                            }
                        }
                    }
                    Err(e) => {
                        warn!("Discovery recv error: {}", e);
                    }
                }
            }
        });

        info!(
            "Discovery service started on port {} for device '{}'",
            self.port, self.device_name
        );
        Ok(())
    }

    fn add_or_update_device(&self, payload: DiscoveryPayload, addr: SocketAddr) {
        let device = DiscoveredDevice {
            device_id: payload.device_id.clone(),
            name: payload.device_name,
            device_type: payload.device_type,
            address: addr,
            port: payload.port,
            capabilities: payload.capabilities,
            last_seen: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs(),
            signal_strength: estimate_signal_strength(&addr),
            protocol_version: 1,
        };

        let mut devices = self.devices.lock().unwrap();
        devices.insert(payload.device_id, device);

        let device_list: Vec<DiscoveredDevice> = devices.values().cloned().collect();
        let _ = self.tx.send(device_list);
    }

    fn cleanup_expired(&self) {
        let mut devices = self.devices.lock().unwrap();
        let before = devices.len();
        devices.retain(|_, d| !d.is_expired());
        if devices.len() != before {
            let device_list: Vec<DiscoveredDevice> = devices.values().cloned().collect();
            let _ = self.tx.send(device_list);
        }
    }

    pub fn get_devices(&self) -> Vec<DiscoveredDevice> {
        let devices = self.devices.lock().unwrap();
        devices
            .values()
            .filter(|d| !d.is_expired())
            .cloned()
            .collect()
    }
}

fn estimate_signal_strength(_addr: &SocketAddr) -> i32 {
    -45
}
