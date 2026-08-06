use aes_gcm::{
    aead::{Aead, KeyInit, OsRng},
    Aes256Gcm, Nonce,
};
use p384::{
    ecdh::EphemeralSecret,
    EncodedPoint, PublicKey as EcdhPublicKey,
};
use rand::RngCore;
use sha2::{Digest, Sha256};

use crate::{ProtocolError, Result};

pub const AES_KEY_SIZE: usize = 32;
pub const AES_NONCE_SIZE: usize = 12;
pub const AES_TAG_SIZE: usize = 16;
pub const ECDH_PUBLIC_KEY_SIZE: usize = 97;

pub struct SessionKeys {
    pub aes_key: [u8; AES_KEY_SIZE],
    pub iv_seed: [u8; AES_NONCE_SIZE],
}

pub struct KeyPair {
    pub secret: EphemeralSecret,
    pub public_key_bytes: [u8; ECDH_PUBLIC_KEY_SIZE],
}

impl KeyPair {
    pub fn generate() -> Self {
        let secret = EphemeralSecret::random(&mut rand::thread_rng());
        let public_key = secret.public_key();
        let encoded = EncodedPoint::from(public_key);
        let mut public_key_bytes = [0u8; ECDH_PUBLIC_KEY_SIZE];
        public_key_bytes.copy_from_bytes(encoded.as_bytes());
        KeyPair {
            secret,
            public_key_bytes,
        }
    }
}

pub fn derive_session_key(
    local_keypair: &KeyPair,
    remote_public_key_bytes: &[u8],
) -> Result<SessionKeys> {
    let remote_point = EncodedPoint::from_bytes(remote_public_key_bytes)
        .map_err(|e| ProtocolError::Crypto(format!("Invalid public key: {}", e)))?;
    let remote_public = EcdhPublicKey::from_sec1_bytes(remote_public_key_bytes)
        .map_err(|e| ProtocolError::Crypto(format!("Invalid SEC1 key: {}", e)))?;

    let shared_secret = local_keypair.secret.diffie_hellman(&remote_public);

    let mut okm = [0u8; AES_KEY_SIZE + AES_NONCE_SIZE];
    hkdf::Hkdf::<Sha256>::new(
        None,
        shared_secret.raw_secret_bytes().as_slice(),
    )
    .expand(b"gesture-share-session-v1", &mut okm)
        .map_err(|e| ProtocolError::Crypto(format!("HKDF expand failed: {}", e)))?;

    let mut aes_key = [0u8; AES_KEY_SIZE];
    let mut iv_seed = [0u8; AES_NONCE_SIZE];
    aes_key.copy_from_slice(&okm[..AES_KEY_SIZE]);
    iv_seed.copy_from_slice(&okm[AES_KEY_SIZE..AES_KEY_SIZE + AES_NONCE_SIZE]);

    Ok(SessionKeys { aes_key, iv_seed })
}

pub fn generate_session_token() -> String {
    let mut bytes = [0u8; 32];
    rand::thread_rng().fill_bytes(&mut bytes);
    bytes.iter().map(|b| format!("{:02x}", b)).collect()
}

pub fn sha256_hash(data: &[u8]) -> [u8; 32] {
    let mut hasher = Sha256::new();
    hasher.update(data);
    hasher.finalize().into()
}

pub fn verify_hash(data: &[u8], expected: &[u8; 32]) -> bool {
    sha256_hash(data) == *expected
}

pub struct AesGcmEncryptor {
    cipher: Aes256Gcm,
}

impl AesGcmEncryptor {
    pub fn new(key: &[u8; AES_KEY_SIZE]) -> Self {
        let cipher = Aes256Gcm::new_from_slice(key).expect("Valid AES-256 key");
        AesGcmEncryptor { cipher }
    }

    pub fn encrypt(&self, plaintext: &[u8], nonce: &[u8; AES_NONCE_SIZE]) -> Result<Vec<u8>> {
        let nonce = Nonce::from_slice(nonce);
        self.cipher
            .encrypt(nonce, plaintext)
            .map_err(|e| ProtocolError::Crypto(format!("Encryption failed: {}", e)))
    }

    pub fn decrypt(&self, ciphertext: &[u8], nonce: &[u8; AES_NONCE_SIZE]) -> Result<Vec<u8>> {
        let nonce = Nonce::from_slice(nonce);
        self.cipher
            .decrypt(nonce, ciphertext)
            .map_err(|e| ProtocolError::Crypto(format!("Decryption failed: {}", e)))
    }

    pub fn encrypt_with_random_nonce(&self, plaintext: &[u8]) -> Result<(Vec<u8>, [u8; AES_NONCE_SIZE])> {
        let mut nonce = [0u8; AES_NONCE_SIZE];
        rand::thread_rng().fill_bytes(&mut nonce);
        let ciphertext = self.encrypt(plaintext, &nonce)?;
        Ok((ciphertext, nonce))
    }
}

pub fn generate_nonce() -> [u8; AES_NONCE_SIZE] {
    let mut nonce = [0u8; AES_NONCE_SIZE];
    rand::thread_rng().fill_bytes(&mut nonce);
    nonce
}
