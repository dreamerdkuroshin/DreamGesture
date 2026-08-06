pub mod crypto;
pub mod discovery;
pub mod protocol;
pub mod transfer;
pub mod gesture;
pub mod direction;

pub use crypto::*;
pub use protocol::*;
pub use transfer::*;
pub use discovery::*;
pub use gesture::*;
pub use direction::*;

pub const PROTOCOL_VERSION: u16 = 1;
pub const DEFAULT_PORT: u16 = 57771;
pub const CHUNK_SIZE: usize = 64 * 1024;
pub const MAX_TRANSFER_SIZE: usize = 500 * 1024 * 1024;
pub const SESSION_TIMEOUT_SECS: u64 = 300;
pub const DISCOVERY_TIMEOUT_SECS: u32 = 30;

#[derive(Debug, Clone, thiserror::Error)]
pub enum ProtocolError {
    #[error("Crypto error: {0}")]
    Crypto(String),
    #[error("Network error: {0}")]
    Network(String),
    #[error("Protocol error: {0}")]
    Protocol(String),
    #[error("Timeout error: {0}")]
    Timeout(String),
    #[error("Transfer error: {0}")]
    Transfer(String),
    #[error("Discovery error: {0}")]
    Discovery(String),
    #[error("Authentication error: {0}")]
    Authentication(String),
}

pub type Result<T> = std::result::Result<T, ProtocolError>;
