package net.common

import db.entities.Group
import db.entities.Product
import net.common.Processor.db
import net.common.ProcessorUtils.Messages.groupMessage
import net.common.ProcessorUtils.Messages.productMessage
import net.common.ProcessorUtils.Parser.id
import net.common.ProcessorUtils.Parser.idAndFloat
import net.common.ProcessorUtils.Parser.idAndInt
import net.common.ProcessorUtils.Parser.idAndName
import net.common.ProcessorUtils.Parser.product
import protocol.Message
import protocol.Message.ServerCommands
import protocol.Message.ServerCommands.ID
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
        fun getGroupName(message: Message): Message {
            val id = message.msg.id()
            val group = db.getGroup(id)
            return groupMessage(group)
        }

        fun addProduct(message: Message): Message {
            val prod = message.msg.product()
            val id = db.insertProduct(prod)
            return Message(ID, msg = "$id")
        }


        fun setProductPrice(message: Message): Message {
            val (id, price) = message.msg.idAndFloat()
            db.setPrice(id, price)
            return Message(ServerCommands.OK, msg = "OK")
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
                split(",").also {
                    assert(it.size != 2)
                }.run { first().toInt() to last() }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.idAndInt(): Pair<Int, Int> {
            return try {
                split(",").also {
                    assert(it.size != 2)
                }.map(String::toInt).run { first() to last() }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.idAndFloat(): Pair<Int, Double> {
            return try {
                split(",").also {
                    assert(it.size != 2)
                }.run { first().toInt() to last().toDouble() }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }

        fun String.product(): Product {
            return try {
                split(",").also {
                    assert(it.size != 2)
                }.run { Product(first(), last().toDouble()) }
            } catch (e: Throwable) {
                throw ParseException(e)
            }
        }
    }

    object Messages {
        fun timeMessage() = Message(ServerCommands.SERVER_TIME, msg = "Time is: ${LocalTime.now()}")
        fun notEnoughItemsMessage() = Message(ServerCommands.ERROR, msg = "Not enough items to remove")
        fun productMessage(product: Product) = Message(ServerCommands.PRODUCT, msg = "$product")
        fun groupMessage(group: Group): Message = Message(ServerCommands.GROUP, msg = "$group")
        fun internalErrorMessage() = Message(ServerCommands.INTERNAL_ERROR, msg = "Report to the dev!")
        fun wrongMsgFormatMessage() = Message(ServerCommands.ERROR, msg = "Wrong message format")
        fun noSuchIdMessage() = Message(ServerCommands.NO_SUCH_PRODUCT, msg = "No such ID in table")
        fun wrongCommand() = Message(ServerCommands.WRONG_COMMAND, msg = "")
        fun byeMessage() = Message(ServerCommands.BYE, msg = "Bye!")
        fun nameTakenMessage() = Message(ServerCommands.ERROR, msg = "Name is already taken")
    }
}