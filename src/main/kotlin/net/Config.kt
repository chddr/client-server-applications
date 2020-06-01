package net

const val ENCRYPTION_THREADS = 3
const val DECRYPTION_THREADS = 3
const val PROCESSOR_THREADS = 3

enum class NetProtocol {
    TCP, UDP
}

enum class Role {
    Server, Client
}


const val SERVER_PORT = 2305
val type = NetProtocol.TCP
