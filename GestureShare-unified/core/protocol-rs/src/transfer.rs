use std::collections::HashSet;
use std::sync::Arc;
use std::time::{Duration, Instant};
use tokio::sync::{Mutex, RwLock};
use tracing::{debug, error, info, warn};

use crate::crypto::{verify_hash, AesGcmEncryptor, SessionKeys, AES_NONCE_SIZE};
use crate::protocol::{
    AckStatus, ChunkAckPayload, DataChunkPayload, MessageType, ProtocolHeader,
    TransferCompletePayload, TransferOfferPayload,
};
use crate::{ProtocolError, Result, CHUNK_SIZE};

#[derive(Debug, Clone, PartialEq)]
pub enum TransferState {
    Idle,
    Offering,
    Accepting,
    Transferring { progress: f32 },
    Completed { duration_ms: u64 },
    Failed { reason: String },
    Cancelled,
}

pub struct TransferSession {
    pub session_id: String,
    pub state: Arc<RwLock<TransferState>>,
    pub keys: SessionKeys,
    pub chunks_sent: Arc<Mutex<u32>>,
    pub chunks_acked: Arc<Mutex<HashSet<u32>>>,
    pub total_chunks: u32,
    pub start_time: Instant,
    pub bytes_transferred: Arc<Mutex<u64>>,
}

impl TransferSession {
    pub fn new(session_id: String, keys: SessionKeys, total_chunks: u32) -> Self {
        TransferSession {
            session_id,
            state: Arc::new(RwLock::new(TransferState::Idle)),
            keys,
            chunks_sent: Arc::new(Mutex::new(0)),
            chunks_acked: Arc::new(Mutex::new(HashSet::new())),
            total_chunks,
            start_time: Instant::now(),
            bytes_transferred: Arc::new(Mutex::new(0)),
        }
    }

    pub async fn set_state(&self, state: TransferState) {
        let mut s = self.state.write().await;
        *s = state;
    }

    pub async fn get_state(&self) -> TransferState {
        self.state.read().await.clone()
    }

    pub async fn record_ack(&self, chunk_index: u32) -> bool {
        let mut acked = self.chunks_acked.lock().await;
        acked.insert(chunk_index);
        acked.len() as u32 >= self.total_chunks
    }

    pub async fn record_bytes(&self, bytes: u64) {
        let mut total = self.bytes_transferred.lock().await;
        *total += bytes;
    }

    pub async fn get_progress(&self) -> f32 {
        let acked = self.chunks_acked.lock().await;
        acked.len() as f32 / self.total_chunks as f32
    }

    pub fn duration_ms(&self) -> u64 {
        self.start_time.elapsed().as_millis() as u64
    }
}

pub struct TransferEngine {
    encryptor: AesGcmEncryptor,
    max_retries: u32,
    retry_delay: Duration,
}

impl TransferEngine {
    pub fn new(session_keys: &SessionKeys) -> Self {
        TransferEngine {
            encryptor: AesGcmEncryptor::new(&session_keys.aes_key),
            max_retries: 3,
            retry_delay: Duration::from_millis(500),
        }
    }

    pub fn create_chunks(
        &self,
        session_id: &str,
        data: &[u8],
    ) -> Result<Vec<DataChunkPayload>> {
        let total_chunks = ((data.len() + CHUNK_SIZE - 1) / CHUNK_SIZE) as u32;
        let mut chunks = Vec::with_capacity(total_chunks as usize);

        for i in 0..total_chunks {
            let start = (i as usize) * CHUNK_SIZE;
            let end = std::cmp::min(start + CHUNK_SIZE, data.len());
            let chunk_data = &data[start..end];

            let nonce = crate::crypto::generate_nonce();
            let encrypted = self.encryptor.encrypt(chunk_data, &nonce)?;
            let chunk_hash = crate::crypto::sha256_hash(chunk_data);

            chunks.push(DataChunkPayload {
                session_id: session_id.to_string(),
                chunk_index: i,
                total_chunks,
                data_length: (end - start) as u32,
                encrypted_data: encrypted,
                nonce,
                chunk_hash,
            });
        }

        Ok(chunks)
    }

    pub fn decrypt_chunk(&self, chunk: &DataChunkPayload) -> Result<Vec<u8>> {
        let plaintext = self
            .encryptor
            .decrypt(&chunk.encrypted_data, &chunk.nonce)?;

        if !verify_hash(&plaintext, &chunk.chunk_hash) {
            return Err(ProtocolError::Transfer(format!(
                "Chunk {} integrity check failed",
                chunk.chunk_index
            )));
        }

        Ok(plaintext)
    }

    pub fn create_offer(
        &self,
        session_id: &str,
        file_name: &str,
        file_size: u64,
        mime_type: &str,
        data: &[u8],
        compression: bool,
    ) -> TransferOfferPayload {
        let total_chunks = ((data.len() + CHUNK_SIZE - 1) / CHUNK_SIZE) as u32;
        let hash = crate::crypto::sha256_hash(data);

        TransferOfferPayload {
            session_id: session_id.to_string(),
            file_name: file_name.to_string(),
            file_size,
            mime_type: mime_type.to_string(),
            total_chunks,
            compression,
            sha256_hash: hash,
        }
    }

    pub fn create_chunk_ack(
        &self,
        session_id: &str,
        chunk_index: u32,
        status: AckStatus,
    ) -> ChunkAckPayload {
        ChunkAckPayload {
            session_id: session_id.to_string(),
            chunk_index,
            status,
        }
    }

    pub fn create_complete(&self, session_id: &str, final_hash: [u8; 32], duration_ms: u64, bytes: u64) -> TransferCompletePayload {
        TransferCompletePayload {
            session_id: session_id.to_string(),
            final_hash,
            duration_ms,
            bytes_transferred: bytes,
        }
    }
}

pub fn compress_data(data: &[u8]) -> Result<Vec<u8>> {
    use flate2::write::ZlibEncoder;
    use flate2::Compression;
    use std::io::Write;

    let mut encoder = ZlibEncoder::new(Vec::new(), Compression::fast());
    encoder
        .write_all(data)
        .map_err(|e| ProtocolError::Transfer(format!("Compression failed: {}", e)))?;
    encoder
        .finish()
        .map_err(|e| ProtocolError::Transfer(format!("Compression finish failed: {}", e)))
}

pub fn decompress_data(data: &[u8]) -> Result<Vec<u8>> {
    use flate2::read::ZlibDecoder;
    use std::io::Read;

    let mut decoder = ZlibDecoder::new(data);
    let mut result = Vec::new();
    decoder
        .read_to_end(&mut result)
        .map_err(|e| ProtocolError::Transfer(format!("Decompression failed: {}", e)))?;
    Ok(result)
}
