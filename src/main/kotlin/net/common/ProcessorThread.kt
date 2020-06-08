package net.common

import net.common.ProcessorUtils.processMessage
import protocol.Message
import protocol.Message.ClientCommands.BYE

/**Server thread reference for sending responses and message*/
class ProcessorThread(private val serverThread: ServerThread, private val message: Message) : Runnable {

    override fun run() {
        println("[[STARTED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread")
        println("$message\n")
        //simulating real work done
        Thread.sleep(200)

        val response = processMessage(message)
        serverThread.send(response)

        if (message.cType == BYE.ordinal)
            serverThread.close()

        println("[[ENDED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread\n")

    }

}
