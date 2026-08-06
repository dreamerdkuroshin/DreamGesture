use std::ffi::CStr;
use std::os::raw::c_char;
use std::slice;

use crate::crypto::{AesGcmEncryptor, KeyPair, SessionKeys, AES_KEY_SIZE, AES_NONCE_SIZE, ECDH_PUBLIC_KEY_SIZE};
use crate::transfer::{compress_data, decompress_data};
use crate::{ProtocolError, Result};

#[repr(C)]
pub struct CKeyPair {
    pub public_key: [u8; ECDH_PUBLIC_KEY_SIZE],
    internal_key: *mut KeyPair,
}

#[repr(C)]
pub struct CEncryptor {
    internal: *mut AesGcmEncryptor,
}

#[no_mangle]
pub extern "C" fn gesture_protocol_init() -> i32 {
    0
}

#[no_mangle]
pub extern "C" fn gesture_protocol_cleanup() {}

#[no_mangle]
pub extern "C" fn key_pair_generate(keypair: *mut CKeyPair) -> i32 {
    if keypair.is_null() {
        return -1;
    }
    let pair = Box::new(KeyPair::generate());
    unsafe {
        (*keypair).public_key = pair.public_key_bytes;
        (*keypair).internal_key = Box::into_raw(pair) as *mut _;
    }
    0
}

#[no_mangle]
pub extern "C" fn key_pair_free(keypair: *mut CKeyPair) {
    if keypair.is_null() {
        return;
    }
    unsafe {
        if !(*keypair).internal_key.is_null() {
            let _ = Box::from_raw((*keypair).internal_key as *mut KeyPair);
            (*keypair).internal_key = std::ptr::null_mut();
        }
    }
}

#[no_mangle]
pub extern "C" fn encryptor_new(keys: *const SessionKeys, encryptor: *mut CEncryptor) -> i32 {
    if keys.is_null() || encryptor.is_null() {
        return -1;
    }
    unsafe {
        let enc = Box::new(AesGcmEncryptor::new(&(*keys).aes_key));
        (*encryptor).internal = Box::into_raw(enc) as *mut _;
    }
    0
}

#[no_mangle]
pub extern "C" fn encryptor_free(encryptor: *mut CEncryptor) {
    if encryptor.is_null() {
        return;
    }
    unsafe {
        if !(*encryptor).internal.is_null() {
            let _ = Box::from_raw((*encryptor).internal as *mut AesGcmEncryptor);
            (*encryptor).internal = std::ptr::null_mut();
        }
    }
}

#[no_mangle]
pub extern "C" fn gesture_sha256(data: *const u8, len: usize, out_hash: *mut u8) {
    if data.is_null() || out_hash.is_null() {
        return;
    }
    let data_slice = unsafe { slice::from_raw_parts(data, len) };
    let hash = crate::crypto::sha256_hash(data_slice);
    unsafe {
        std::ptr::copy_nonoverlapping(hash.as_ptr(), out_hash, 32);
    }
}

#[no_mangle]
pub extern "C" fn gesture_verify_hash(data: *const u8, len: usize, expected: *const u8) -> i32 {
    if data.is_null() || expected.is_null() {
        return -1;
    }
    let data_slice = unsafe { slice::from_raw_parts(data, len) };
    let expected_slice = unsafe { slice::from_raw_parts(expected, 32) };
    let mut expected_array = [0u8; 32];
    expected_array.copy_from_slice(expected_slice);
    if crate::crypto::verify_hash(data_slice, &expected_array) {
        1
    } else {
        0
    }
}

#[no_mangle]
pub extern "C" fn gesture_generate_session_token() -> *mut c_char {
    let token = crate::crypto::generate_session_token();
    let c_string = std::ffi::CString::new(token).unwrap();
    c_string.into_raw()
}

#[no_mangle]
pub extern "C" fn gesture_free_string(s: *mut c_char) {
    if !s.is_null() {
        unsafe {
            let _ = std::ffi::CString::from_raw(s);
        }
    }
}

#[no_mangle]
pub extern "C" fn gesture_free_buffer(buf: *mut u8) {
    if !buf.is_null() {
        unsafe {
            let _ = Box::from_raw(buf);
        }
    }
}

#[no_mangle]
pub extern "C" fn compress_data_ffi(
    data: *const u8,
    len: usize,
    out_compressed: *mut *mut u8,
    out_len: *mut usize,
) -> i32 {
    if data.is_null() || out_compressed.is_null() || out_len.is_null() {
        return -1;
    }
    let data_slice = unsafe { slice::from_raw_parts(data, len) };
    match compress_data(data_slice) {
        Ok(compressed) => {
            unsafe {
                *out_len = compressed.len();
                *out_compressed = compressed.as_ptr() as *mut u8;
                std::mem::forget(compressed);
            }
            0
        }
        Err(_) => -1,
    }
}

#[no_mangle]
pub extern "C" fn decompress_data_ffi(
    data: *const u8,
    len: usize,
    out_decompressed: *mut *mut u8,
    out_len: *mut usize,
) -> i32 {
    if data.is_null() || out_decompressed.is_null() || out_len.is_null() {
        return -1;
    }
    let data_slice = unsafe { slice::from_raw_parts(data, len) };
    match decompress_data(data_slice) {
        Ok(decompressed) => {
            unsafe {
                *out_len = decompressed.len();
                *out_decompressed = decompressed.as_ptr() as *mut u8;
                std::mem::forget(decompressed);
            }
            0
        }
        Err(_) => -1,
    }
}
