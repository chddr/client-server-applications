package net.interfaces

import net.impl.Processor
import net.protocol.Message
import net.protocol.Packet
import java.io.Closeable

abstract class ServerThread : Runnable, Closeable {

    private var inCounter: Long = 0
    private var outCounter: Long = 0

    fun process(packet: Packet) {
        if (packet.msgID == inCounter) {
            inCounter++
            Processor.process(this, packet)
        } else {
            send(Packet(
                    0,
                    outCounter++,
                    Message(
                            Message.ServerCommandTypes.RESPONSE_RESEND.ordinal,
                            packet.msg.userID,
                            "Expected: $inCounter"
                    )
            ))
        }
    }

    fun send(msg: Message) = send(Packet(0, outCounter++, msg))

    abstract fun send(packet: Packet)
    abstract override fun close()
    abstract fun processPackets()
}