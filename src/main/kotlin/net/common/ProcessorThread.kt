package net.common

import db.DaoProduct.*
import db.entities.Group
import db.entities.Product
import net.common.Processor.db
import protocol.Message
import protocol.Message.ClientCommands
import protocol.Message.ClientCommands.*
import protocol.Message.ServerCommands.*
import protocol.Message.ServerCommands.BYE
import java.time.LocalTime

/**Server thread reference for sending responses and message*/
class ProcessorThread(private val serverThread: ServerThread, private val message: Message) : Runnable {

    override fun run() {
        println("[[STARTED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread")
        println("$message\n")
        //simulating real work done
        Thread.sleep(200)

        val response = process()
        serverThread.send(response)

        if (message.cType == ClientCommands.BYE.ordinal)
            serverThread.close()

        println("[[ENDED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread\n")

    }

    private fun process(): Message {
        return if (message.cType in ClientCommands)
            try {
                chooseResponse()
            } catch (e: Exception) {
                internalErrorMessage()
            }
        else wrongCommand()
    }

    private fun chooseResponse(): Message {
        return when (ClientCommands[message.cType]) {
            GET_PRODUCT -> getProduct(message)
            ADD_GROUP -> internalErrorMessage() //TODO
            ADD_PRODUCT -> addProduct(message)
            INCREASE_PRODUCT_COUNT -> increaseProductCount(message)
            DECREASE_PRODUCT_COUNT -> decreaseProductCount(message)
            SET_PRODUCT_PRICE -> setProductPrice(message)
            ClientCommands.BYE -> byeMessage()
            GET_TIME -> timeMessage()
            GET_GROUP_NAME -> getGroupName(message)
            CHANGE_PRODUCT_NAME -> TODO()
            CHANGE_GROUP_NAME -> TODO()
        }
    }

    private fun getGroupName(message: Message): Message {
        return try {
            val id = message.msg.toInt()
            val group = db.getGroup(id)
            groupMessage(group)
        } catch (e: NumberFormatException) {
            return wrongMsgFormatMessage()
        } catch (e: NoSuchGroupIdException) {
            return noSuchIdMessage()
        }
    }

    private fun addProduct(message: Message): Message {
        return try {
            val prod = productFromString(message.msg)
            val id = db.insertProduct(prod)
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
        } catch (e: NoSuchProductIdException) {
            noSuchIdMessage()
        } catch (e: NotEnoughItemsException) {
            notEnoughItemsMessage()
        }

    }

    private fun decreaseProductCount(message: Message): Message {
        return try {
            val (id, decrement) = idAndInt(message.msg)
            db.removeItems(id, decrement)
            val product = db.getProduct(id)
            productMessage(product)
        } catch (e: ParseException) {
            wrongMsgFormatMessage()
        } catch (e: NoSuchProductIdException) {
            noSuchIdMessage()
        } catch (e: NotEnoughItemsException) {
            notEnoughItemsMessage()
        }
    }

    private fun increaseProductCount(message: Message): Message {
        return try {
            val (id, increment) = idAndInt(message.msg)
            db.addItems(id, increment)
            val product = db.getProduct(id)
            productMessage(product)
        } catch (e: ParseException) {
            wrongMsgFormatMessage()
        } catch (e: NoSuchProductIdException) {
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

    private fun getProduct(message: Message): Message {
        val id = try {
            message.msg.toInt()
        } catch (e: NumberFormatException) {
            return wrongMsgFormatMessage()
        }
        val product = db.getProduct(id)

        return productMessage(product)
    }

    private fun timeMessage() = Message(SERVER_TIME, msg = "Time is: ${LocalTime.now()}")
    private fun notEnoughItemsMessage() = Message(ERROR, msg = "Not enough items to remove")
    private fun productMessage(product: Product) = Message(PRODUCT, msg = "$product")
    private fun groupMessage(group: Group): Message = Message(GROUP, msg = "$group")
    private fun internalErrorMessage() = Message(INTERNAL_ERROR, msg = "Report to the dev!")
    private fun wrongMsgFormatMessage() = Message(ERROR, msg = "Wrong message format")
    private fun noSuchIdMessage() = Message(NO_SUCH_PRODUCT, msg = "No such ID in table")
    private fun wrongCommand() = Message(WRONG_COMMAND, msg = "")
    private fun byeMessage() = Message(BYE, msg = "Bye!")
    private fun nameTakenMessage() = Message(ERROR, msg = "Name is already taken")

}
