use serde::{Deserialize, Serialize};

pub const MAGIC_BYTES: &[u8; 4] = b"GSHR";
pub const PROTOCOL_VERSION: u16 = 1;

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum MessageType {
    DiscoveryBroadcast,
    DiscoveryResponse,
    SessionRequest,
    SessionAccept,
    SessionReject,
    KeyExchange,
    TransferOffer,
    TransferAccept,
    TransferReject,
    DataChunk,
    ChunkAck,
    TransferComplete,
    TransferCancel,
    Ping,
    Pong,
    Error,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ProtocolHeader {
    pub magic: [u8; 4],
    pub version: u16,
    pub message_type: MessageType,
    pub sequence_id: u64,
    pub payload_length: u32,
    pub timestamp: u64,
}

impl ProtocolHeader {
    pub fn new(message_type: MessageType, sequence_id: u64, payload_length: u32) -> Self {
        ProtocolHeader {
            magic: *MAGIC_BYTES,
            version: PROTOCOL_VERSION,
            message_type,
            sequence_id,
            payload_length,
            timestamp: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs(),
        }
    }

    pub fn to_bytes(&self) -> Vec<u8> {
        let mut bytes = Vec::with_capacity(32);
        bytes.extend_from_slice(&self.magic);
        bytes.extend_from_slice(&self.version.to_be_bytes());
        bytes.extend_from_slice(&(self.message_type as u16).to_be_bytes());
        bytes.extend_from_slice(&self.sequence_id.to_be_bytes());
        bytes.extend_from_slice(&self.payload_length.to_be_bytes());
        bytes.extend_from_slice(&self.timestamp.to_be_bytes());
        bytes
    }

    pub fn from_bytes(bytes: &[u8]) -> Option<Self> {
        if bytes.len() < 32 {
            return None;
        }
        let magic = [bytes[0], bytes[1], bytes[2], bytes[3]];
        if &magic != MAGIC_BYTES {
            return None;
        }
        let version = u16::from_be_bytes([bytes[4], bytes[5]]);
        let type_raw = u16::from_be_bytes([bytes[6], bytes[7]]);
        let message_type = match type_raw {
            0 => MessageType::DiscoveryBroadcast,
            1 => MessageType::DiscoveryResponse,
            2 => MessageType::SessionRequest,
            3 => MessageType::SessionAccept,
            4 => MessageType::SessionReject,
            5 => MessageType::KeyExchange,
            6 => MessageType::TransferOffer,
            7 => MessageType::TransferAccept,
            8 => MessageType::TransferReject,
            9 => MessageType::DataChunk,
            10 => MessageType::ChunkAck,
            11 => MessageType::TransferComplete,
            12 => MessageType::TransferCancel,
            13 => MessageType::Ping,
            14 => MessageType::Pong,
            _ => MessageType::Error,
        };
        let sequence_id = u64::from_be_bytes([
            bytes[8], bytes[9], bytes[10], bytes[11],
            bytes[12], bytes[13], bytes[14], bytes[15],
        ]);
        let payload_length = u32::from_be_bytes([bytes[16], bytes[17], bytes[18], bytes[19]]);
        let timestamp = u64::from_be_bytes([
            bytes[20], bytes[21], bytes[22], bytes[23],
            bytes[24], bytes[25], bytes[26], bytes[27],
        ]);
        Some(ProtocolHeader {
            magic,
            version,
            message_type,
            sequence_id,
            payload_length,
            timestamp,
        })
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DiscoveryPayload {
    pub device_id: String,
    pub device_name: String,
    pub device_type: DeviceType,
    pub capabilities: Vec<String>,
    pub port: u16,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum DeviceType {
    AndroidPhone,
    AndroidTablet,
    WindowsDesktop,
    WindowsLaptop,
    LinuxDesktop,
    LinuxLaptop,
    MacOSDesktop,
    MacOSLaptop,
    Unknown,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SessionPayload {
    pub session_id: String,
    pub public_key: Vec<u8>,
    pub device_id: String,
    pub ephemeral_id: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TransferOfferPayload {
    pub session_id: String,
    pub file_name: String,
    pub file_size: u64,
    pub mime_type: String,
    pub total_chunks: u32,
    pub compression: bool,
    pub sha256_hash: [u8; 32],
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DataChunkPayload {
    pub session_id: String,
    pub chunk_index: u32,
    pub total_chunks: u32,
    pub data_length: u32,
    pub encrypted_data: Vec<u8>,
    pub nonce: [u8; 12],
    pub chunk_hash: [u8; 32],
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ChunkAckPayload {
    pub session_id: String,
    pub chunk_index: u32,
    pub status: AckStatus,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum AckStatus {
    Ok,
    Resend,
    Error(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TransferCompletePayload {
    pub session_id: String,
    pub final_hash: [u8; 32],
    pub duration_ms: u64,
    pub bytes_transferred: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ErrorPayload {
    pub code: u16,
    pub message: String,
}

impl ProtocolHeader {
    pub fn validate(&self) -> bool {
        self.magic == *MAGIC_BYTES && self.version == PROTOCOL_VERSION
    }
}
