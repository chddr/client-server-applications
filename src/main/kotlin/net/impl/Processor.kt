package net.impl

import net.PROCESSOR_THREADS
import net.interfaces.ServerThread
import net.protocol.Message
import net.protocol.Message.ServerCommands.*
import pr4.DaoProduct
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Processor(private val serverThread: ServerThread, private val message: Message) : Runnable {

    companion object {
        private val service = Executors.newFixedThreadPool(PROCESSOR_THREADS)
        private val db = DaoProduct("file.db")

        fun process(serverThread: ServerThread, message: Message) {
            service.submit(Processor(serverThread, message))
        }

        fun ExecutorService.waitForStop() {
            shutdown()
            while (!isTerminated) {
                awaitTermination(10, TimeUnit.SECONDS)
                println("Service is waiting for shutdown")
            }
        }

        fun waitForProcessorStop() = service.waitForStop()

    }


    override fun run() {
        println("[[STARTED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread")
        println("$message\n")

        //simulating real work done
        Thread.sleep(200)

        //val response = if (message.cType in ClientCommands)
        //    when (ClientCommands[message.cType]) {
        //        ClientCommands.GET_PRODUCT_COUNT -> getProductCount()
        //        ClientCommands.ADD_GROUP -> TODO()
        //        ClientCommands.ADD_PRODUCT_TO_GROUP -> TODO()
        //        ClientCommands.INCREASE_PRODUCT_COUNT -> increaseProductCount()
        //        ClientCommands.DECREASE_PRODUCT_COUNT -> TODO()
        //        ClientCommands.SET_PRODUCT_PRICE -> TODO()
        //        ClientCommands.CLIENT_BYE -> TODO()
        //    }
        //else wrongCommand()


        val msg: String
        val cType: Int

        when (message.msg) {
            "hello" -> {
                msg = "Hello from server, it's ${LocalDateTime.now().toLocalTime()}"
                cType = OK.ordinal
            }
            else -> {
                msg = "BYE"
                cType = BYE.ordinal
            }
        }


        serverThread.send(Message(cType, msg = msg))

        if (message.cType == BYE.ordinal)
            serverThread.close()
        println("[[ENDED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread\n")

    }

    private fun increaseProductCount(): Message {
        return try {
            val (id, increment) = idIncrement(message.msg)
            db.addItems(id, increment)
            val amount = db.amount(id)
            idAmountMessage(id, amount!!)
        } catch (e: Throwable) {
            when (e) {
                is java.lang.IllegalArgumentException -> wrongMsgFormatMessage()
                is java.lang.NumberFormatException -> wrongIdMessage()
                is Exception -> noSuchIdMessage() //TODO change here after changed in DaoProduct
                else -> internalErrorMessage()
            }
        }
    }


    private fun idIncrement(string: String) = string.split(":").map(String::toInt).run { first() to last() }

    private fun getProductCount(): Message {
        val id = try {
            message.msg.toInt()
        } catch (e: NumberFormatException) {
            return wrongIdMessage()
        }
        val amount = db.amount(id)

        return if (amount == null)
            noSuchIdMessage()
        else
            idAmountMessage(id, amount)
    }

    private fun idAmountMessage(id: Int, amount: Int) = Message(ID_PRODUCT_COUNT, msg = "$id:$amount")
    private fun internalErrorMessage() = Message(INTERNAL_ERROR, msg = "Report to the dev!")
    private fun wrongMsgFormatMessage() = Message(ERROR, msg = "Wrong message format")
    private fun noSuchIdMessage() = Message(NO_SUCH_PRODUCT, msg = "No such ID in table")
    private fun wrongIdMessage() = Message(ERROR, msg = "ID should be a number")
    private fun wrongCommand() = Message(WRONG_COMMAND, msg = "")

}