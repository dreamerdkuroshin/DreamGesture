use gesture_protocol::crypto::*;

#[test]
fn test_generate_symmetric_key() {
    let key = generate_symmetric_key();
    assert_eq!(key.as_slice().len(), AES_KEY_SIZE);
    assert!(key.as_slice().iter().any(|&b| b != 0));
}

#[test]
fn test_encrypt_decrypt_roundtrip() {
    let key_bytes = generate_symmetric_key();
    let key_array: [u8; AES_KEY_SIZE] = key_bytes.as_slice().try_into().unwrap();
    let encryptor = AesGcmEncryptor::new(&key_array);
    let plaintext = b"Hello, GestureShare!";
    let nonce = generate_nonce();

    let ciphertext = encryptor.encrypt(plaintext, &nonce).unwrap();
    let decrypted = encryptor.decrypt(&ciphertext, &nonce).unwrap();

    assert_eq!(decrypted, plaintext);
}

#[test]
fn test_encrypt_with_random_nonce() {
    let key_bytes = generate_symmetric_key();
    let key_array: [u8; AES_KEY_SIZE] = key_bytes.as_slice().try_into().unwrap();
    let encryptor = AesGcmEncryptor::new(&key_array);
    let plaintext = b"Test data for encryption";

    let (ciphertext1, nonce1) = encryptor.encrypt_with_random_nonce(plaintext).unwrap();
    let (ciphertext2, nonce2) = encryptor.encrypt_with_random_nonce(plaintext).unwrap();

    assert_ne!(nonce1, nonce2);
    assert_ne!(ciphertext1, ciphertext2);

    let decrypted = encryptor.decrypt(&ciphertext1, &nonce1).unwrap();
    assert_eq!(decrypted, plaintext);
}

#[test]
fn test_sha256() {
    let data = b"test data";
    let hash = sha256_hash(data);
    assert_eq!(hash.len(), 32);
    assert!(verify_hash(data, &hash));
}

#[test]
fn test_verify_hash_mismatch() {
    let data1 = b"test data 1";
    let data2 = b"test data 2";
    let hash = sha256_hash(data1);
    assert!(!verify_hash(data2, &hash));
}

#[test]
fn test_session_token() {
    let token = generate_session_token();
    assert_eq!(token.len(), 64);
    assert!(token.chars().all(|c| c.is_ascii_hexdigit()));
}

#[test]
fn test_generate_nonce() {
    let nonce1 = generate_nonce();
    let nonce2 = generate_nonce();
    assert_eq!(nonce1.len(), AES_NONCE_SIZE);
    assert_eq!(nonce2.len(), AES_NONCE_SIZE);
}
