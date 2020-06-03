package net

const val SERVER_THREADS = 6
const val ENCRYPTION_THREADS = 3
const val DECRYPTION_THREADS = 3
const val PROCESSOR_THREADS = 3

enum class NetProtocol {
    TCP, UDP
}

const val SOCKET_TIMEOUT_TIME_MILLISECONDS = 20_000 //TODO change when in production, low value useful for tests
const val HOST = "localhost"
const val SERVER_PORT = 2305
val type = NetProtocol.UDP
