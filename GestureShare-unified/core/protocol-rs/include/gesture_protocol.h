#ifndef GESTURE_PROTOCOL_H
#define GESTURE_PROTOCOL_H

#include <stdint.h>
#include <stddef.h>

#define GESTURE_PROTOCOL_VERSION 1
#define GESTURE_PROTOCOL_DEFAULT_PORT 57771
#define GESTURE_PROTOCOL_AES_KEY_SIZE 32
#define GESTURE_PROTOCOL_AES_NONCE_SIZE 12
#define GESTURE_PROTOCOL_AES_TAG_SIZE 16
#define GESTURE_PROTOCOL_ECDH_PUBLIC_KEY_SIZE 97
#define GESTURE_PROTOCOL_SHA256_SIZE 32
#define GESTURE_PROTOCOL_MAX_TRANSFER_SIZE (500 * 1024 * 1024)
#define GESTURE_PROTOCOL_CHUNK_SIZE (64 * 1024)

typedef enum {
    GESTURE_OK = 0,
    GESTURE_ERROR_CRYPTO = 1,
    GESTURE_ERROR_NETWORK = 2,
    GESTURE_ERROR_PROTOCOL = 3,
    GESTURE_ERROR_TIMEOUT = 4,
    GESTURE_ERROR_TRANSFER = 5,
    GESTURE_ERROR_DISCOVERY = 6,
    GESTURE_ERROR_AUTH = 7,
} GestureError;

typedef struct {
    uint8_t key[GESTURE_PROTOCOL_AES_KEY_SIZE];
    uint8_t iv_seed[GESTURE_PROTOCOL_AES_NONCE_SIZE];
} SessionKeys;

typedef struct {
    uint8_t public_key[GESTURE_PROTOCOL_ECDH_PUBLIC_KEY_SIZE];
    void* internal;
} KeyPair;

typedef struct {
    void* internal;
} Encryptor;

typedef struct {
    void* internal;
} DiscoveryService;

typedef void (*DeviceCallback)(const char* device_id, const char* device_name, const char* address, int32_t port, void* user_data);

GestureError gesture_protocol_init(void);
void gesture_protocol_cleanup(void);

GestureError key_pair_generate(KeyPair* keypair);
void key_pair_free(KeyPair* keypair);
GestureError derive_session_key(const KeyPair* local, const uint8_t* remote_public_key, SessionKeys* out_keys);

GestureError encryptor_new(const SessionKeys* keys, Encryptor* out_encryptor);
void encryptor_free(Encryptor* encryptor);
GestureError encryptor_encrypt(const Encryptor* encryptor, const uint8_t* plaintext, size_t plaintext_len, const uint8_t nonce[GESTURE_PROTOCOL_AES_NONCE_SIZE], uint8_t** out_ciphertext, size_t* out_ciphertext_len);
GestureError encryptor_decrypt(const Encryptor* encryptor, const uint8_t* ciphertext, size_t ciphertext_len, const uint8_t nonce[GESTURE_PROTOCOL_AES_NONCE_SIZE], uint8_t** out_plaintext, size_t* out_plaintext_len);

void gesture_free_buffer(uint8_t* buffer);

void gesture_sha256(const uint8_t* data, size_t len, uint8_t out_hash[GESTURE_PROTOCOL_SHA256_SIZE]);
int gesture_verify_hash(const uint8_t* data, size_t len, const uint8_t expected[GESTURE_PROTOCOL_SHA256_SIZE]);

char* gesture_generate_session_token(void);
void gesture_free_string(char* s);

GestureError compress_data(const uint8_t* data, size_t len, uint8_t** out_compressed, size_t* out_compressed_len);
GestureError decompress_data(const uint8_t* data, size_t len, uint8_t** out_decompressed, size_t* out_decompressed_len);

#endif /* GESTURE_PROTOCOL_H */
