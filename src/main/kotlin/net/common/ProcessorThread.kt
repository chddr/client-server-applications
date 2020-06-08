package net.common

import net.common.ProcessorUtils.Messages.wrongCommand
import net.common.ProcessorUtils.catchException
import net.common.ProcessorUtils.chooseResponse
import protocol.Message
import protocol.Message.ClientCommands
import protocol.Message.ClientCommands.BYE

/**Server thread reference for sending responses and message*/
class ProcessorThread(private val serverThread: ServerThread, private val message: Message) : Runnable {

    override fun run() {
        println("[[STARTED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread")
        println("$message\n")
        //simulating real work done
        Thread.sleep(200)

        val response = process()
        serverThread.send(response)

        if (message.cType == BYE.ordinal)
            serverThread.close()

        println("[[ENDED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread\n")

    }

    private fun process(): Message {
        return if (message.cType in ClientCommands)
            try {
                chooseResponse(message)
            } catch (e: Throwable) {
                catchException(e)
            }
        else wrongCommand()
    }

}
