package net

import net.packet.EncryptorDecryptor
import kotlin.concurrent.thread

object Decryptor {
    fun decrypt(message: ByteArray) {
        thread(start = true) {

            val packet = EncryptorDecryptor.decrypt(message)
        }
    }

}