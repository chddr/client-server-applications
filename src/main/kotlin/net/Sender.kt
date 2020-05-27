package net

import net.packet.Message
import java.net.InetAddress
import kotlin.concurrent.thread

object Sender {

    fun sendMessage(message: Message, target: InetAddress) {
        thread(start = true) {
            println("message $message is being sent to address $target")
        }
    }
}