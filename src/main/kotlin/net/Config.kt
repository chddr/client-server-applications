package net

import net.NetProtocol.TCP

const val SERVER_THREADS = 6
const val ENCRYPTION_THREADS = 3
const val DECRYPTION_THREADS = 3
const val PROCESSOR_THREADS = 3

enum class NetProtocol {
    TCP, UDP
}

enum class Role {
    Server, Client
}


const val SOCKET_TIMEOUT_TIME_MILLISECONDS = 20_000
const val HOST = "localhost"
const val SERVER_PORT = 2305
val type = TCP
