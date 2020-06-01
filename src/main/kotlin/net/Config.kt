package net

import net.NetProtocol.TCP

const val ENCRYPTION_THREADS = 3
const val DECRYPTION_THREADS = 3
const val PROCESSOR_THREADS = 3

enum class NetProtocol {
    TCP, UDP
}

enum class Role {
    Server, Client
}

const val HOST = "localhost"
const val SERVER_PORT = 2305
val type = TCP
