package net.common

import db.entities.Group
import db.entities.Product
import net.common.Processor.db
import net.common.ProcessorUtils.Messages.groupMessage
import net.common.ProcessorUtils.Messages.productMessage
import net.common.ProcessorUtils.Messages.successfulDeletionMessage
import net.common.ProcessorUtils.Parser.id
import net.common.ProcessorUtils.Parser.idAndDouble
import net.common.ProcessorUtils.Parser.idAndInt
import net.common.ProcessorUtils.Parser.idAndName
import net.common.ProcessorUtils.Parser.product
import protocol.Message
import protocol.Message.ServerCommands.*
import java.time.LocalTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

object ProcessorUtils {

    fun ExecutorService.waitForStop(what: String) {
        shutdown()
        while (!isTerminated) {
            println("$what is waiting for shutdown")
            awaitTermination(10, TimeUnit.SECONDS)
        }
    }

    object MessageGenerators {
        fun getGroup(message: Message): Message {
            val id = message.msg.id()
            val group = db.getGroup(id)
            return groupMessage(group)
        }

        fun addProduct(message: Message): Message {
            val prod = message.msg.product()
            val id = db.insertProduct(prod)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun setProductPrice(message: Message): Message {
            val (id, price) = message.msg.idAndDouble()
            db.setPrice(id, price)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun decreaseProductCount(message: Message): Message {
            val (id, decrement) = message.msg.idAndInt()
            db.removeItems(id, decrement)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun increaseProductCount(message: Message): Message {
            val (id, increment) = message.msg.idAndInt()
            db.addItems(id, increment)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun getProduct(message: Message): Message {
            val id = message.msg.id()
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun changeProductName(message: Message): Message {
            val (id, name) = message.msg.idAndName()
            db.changeProductName(id, name)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun changeGroupName(message: Message): Message {
            val (id, name) = message.msg.idAndName()
            db.changeGroupName(id, name)
            val group = db.getGroup(id)
            return groupMessage(group)
        }

        fun addGroup(message: Message): Message {
            val name = message.msg
            val id = db.addGroup(name)
            val group = db.getGroup(id)
            return groupMessage(group)
        }

        fun removeGroup(message: Message): Message {
            val id = message.msg.id()
            db.deleteGroup(id)
            return successfulDeletionMessage(id)
        }

        fun removeProduct(message: Message): Message {
            val id = message.msg.id()
            db.deleteProduct(id)
            return successfulDeletionMessage(id)
        }


    }

    object Parser {
        class ParseException(e: Throwable) : Exception(e)

        fun String.id(): Int {
            return try {
                toInt()
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.idAndName(): Pair<Int, String> {
            return try {
                split(":").map { it.trim() }.also {
                    assert(it.size == 2)
                }.run { first().toInt() to last() }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.idAndInt(): Pair<Int, Int> {
            return try {
                split(":").map { it.trim() }.also {
                    assert(it.size == 2)
                }.map(String::toInt).run { first() to last() }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.idAndDouble(): Pair<Int, Double> {
            return try {
                split(":").map { it.trim() }.also {
                    assert(it.size == 2)
                }.run { first().toInt() to last().toDouble() }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.product(): Product {
            return try {
                split(":").map { it.trim() }.also {
                    assert(it.size == 2)
                }.run { Product(first(), last().toDouble()) }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }
    }

    object Messages {
        fun timeMessage() = Message(SERVER_TIME, msg = "Time is: ${LocalTime.now()}")
        fun notEnoughItemsMessage() = Message(NOT_ENOUGH_ITEMS_ERROR, msg = "Not enough items to remove")
        fun productMessage(product: Product) = Message(PRODUCT, msg = "$product")
        fun groupMessage(group: Group): Message = Message(GROUP, msg = "$group")
        fun internalErrorMessage() = Message(INTERNAL_ERROR, msg = "Report to the dev!")
        fun wrongMsgFormatMessage() = Message(WRONG_MESSAGE_FORMAT_ERROR, msg = "Wrong message format")
        fun noSuchIdMessage() = Message(NO_SUCH_ID_ERROR, msg = "No such ID in table")
        fun wrongCommand() = Message(WRONG_COMMAND, msg = "")
        fun byeMessage() = Message(BYE, msg = "Bye!")
        fun wrongNameFormatMessage() = Message(WRONG_NAME_ERROR, msg = "Name you entered is in wrong format")
        fun nameTakenMessage() = Message(NAME_TAKEN_ERROR, msg = "Name is already taken")
        fun successfulDeletionMessage(id: Int) = Message(SUCCESSFUL_DELETION, msg = "Successfully deleted $id")
        fun nonEmptyProductMessage() = Message(NON_EMPTY_PRODUCT_ERROR, msg = "Can't delete product where quantity != 0")
    }
}