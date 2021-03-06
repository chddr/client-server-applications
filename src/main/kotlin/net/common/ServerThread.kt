package net.common

import protocol.Message
import protocol.Packet
import java.io.Closeable

abstract class ServerThread : Runnable, Closeable {

    private var inCounter: Long = 0
    private var outCounter: Long = 0

    fun process(packet: Packet) {
        if (packet.msgID == inCounter) {
            inCounter++
            Processor.process(this, packet.msg)
        } else {
            send(Packet(
                    0,
                    outCounter++,
                    Message(
                            Message.ServerCommands.RESEND,
                            packet.msg.userID,
                            "Expected: $inCounter"
                    )
            ))
        }
    }

    fun send(msg: Message) = send(Packet(0, outCounter++, msg))

    abstract fun send(packet: Packet)
    override fun close() {
        send(Message(
                Message.ServerCommands.SERVER_BYE,
                0,
                "Connection timed out"
        ))
    } //TODO proper timeout for single thread
    abstract fun processPackets()
}