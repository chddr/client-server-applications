package net.common

import net.PROCESSOR_THREADS
import protocol.Message
import protocol.Message.ClientCommands
import protocol.Message.ClientCommands.*
import protocol.Message.ServerCommands.*
import protocol.Message.ServerCommands.BYE
import db.DaoProduct
import db.DaoProduct.*
import db.entities.Product
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


        val response = if (message.cType in ClientCommands)
            try {
                when (ClientCommands[message.cType]) {
                    GET_PRODUCT_COUNT -> getProductCount(message)
                    ADD_GROUP -> internalErrorMessage() //TODO
                    ADD_PRODUCT -> addProduct(message)
                    INCREASE_PRODUCT_COUNT -> increaseProductCount(message)
                    DECREASE_PRODUCT_COUNT -> decreaseProductCount(message)
                    SET_PRODUCT_PRICE -> setProductPrice(message)
                    ClientCommands.BYE -> byeMessage()
                }
            } catch (e: Exception) {
                internalErrorMessage()
            }
        else wrongCommand()


        serverThread.send(response)

        if (message.cType == ClientCommands.BYE.ordinal) {
            serverThread.close()
        }
        println("[[ENDED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread\n")

    }

    private fun addProduct(message: Message): Message {
        return try {
            val prod = productFromString(message.msg)
            val id = db.insert(prod)
            Message(ID, msg = "$id")
        } catch (e: ParseException) {
            wrongMsgFormatMessage()
        } catch (e: NameTakenException) {
            nameTakenMessage()
        }
    }


    private fun setProductPrice(message: Message): Message {
        return try {
            val (id, price) = idAndFloat(message.msg)
            db.setPrice(id, price)
            Message(OK, msg = "OK")
        } catch (e: ParseException) {
            wrongMsgFormatMessage()
        } catch (e: NoSuchIdException) {
            noSuchIdMessage()
        } catch (e: NotEnoughItemsException) {
            notEnoughItemsMessage()
        }

    }

    private fun decreaseProductCount(message: Message): Message {
        return try {
            val (id, decrement) = idAndInt(message.msg)
            db.removeItems(id, decrement)
            val amount = db.amount(id)
            idAmountMessage(id, amount!!)
        } catch (e: ParseException) {
            wrongMsgFormatMessage()
        } catch (e: NoSuchIdException) {
            noSuchIdMessage()
        } catch (e: NotEnoughItemsException) {
            notEnoughItemsMessage()
        }
    }

    private fun increaseProductCount(message: Message): Message {
        return try {
            val (id, increment) = idAndInt(message.msg)
            db.addItems(id, increment)
            val amount = db.amount(id)
            idAmountMessage(id, amount!!)
        } catch (e: ParseException) {
            wrongMsgFormatMessage()
        } catch (e: NoSuchIdException) {
            noSuchIdMessage()
        }
    }

    class ParseException(e: Throwable) : Exception(e)

    private fun idAndInt(string: String): Pair<Int, Int> {
        return try {
            string.split(":").also {
                assert(it.size != 2)
            }.map(String::toInt).run { first() to last() }
        } catch (e: Throwable) {
            throw ParseException(e)
        }
    }

    private fun idAndFloat(string: String): Pair<Int, Double> {
        return try {
            string.split(":").also {
                assert(it.size != 2)
            }.run { first().toInt() to last().toDouble() }
        } catch (e: Throwable) {
            throw ParseException(e)
        }
    }

    private fun productFromString(string: String): Product {
        return try {
            string.split(":").also {
                assert(it.size != 2)
            }.run { Product(first(), last().toDouble()) }
        } catch (e: Throwable) {
            throw ParseException(e)
        }
    }

    private fun getProductCount(message: Message): Message {
        val id = try {
            message.msg.toInt()
        } catch (e: NumberFormatException) {
            return wrongMsgFormatMessage()
        }
        val amount = db.amount(id)

        return if (amount == null)
            noSuchIdMessage()
        else
            idAmountMessage(id, amount)
    }

    private fun notEnoughItemsMessage() = Message(ERROR, msg = "Not enough items to remove")
    private fun idAmountMessage(id: Int, amount: Int) = Message(ID_PRODUCT_COUNT, msg = "$id:$amount")
    private fun internalErrorMessage() = Message(INTERNAL_ERROR, msg = "Report to the dev!")
    private fun wrongMsgFormatMessage() = Message(ERROR, msg = "Wrong message format")
    private fun noSuchIdMessage() = Message(NO_SUCH_PRODUCT, msg = "No such ID in table")
    private fun wrongCommand() = Message(WRONG_COMMAND, msg = "")
    private fun byeMessage() = Message(BYE, msg = "Bye!")
    private fun nameTakenMessage() = Message(ERROR, msg = "Name is already taken")

}
