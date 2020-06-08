package net.common

import db.DaoProduct
import db.entities.Group
import db.entities.Product
import net.common.Processor.db
import net.common.ProcessorUtils.MessageGenerators.addGroup
import net.common.ProcessorUtils.MessageGenerators.addProduct
import net.common.ProcessorUtils.MessageGenerators.changeGroupName
import net.common.ProcessorUtils.MessageGenerators.changeProductName
import net.common.ProcessorUtils.MessageGenerators.decreaseProductCount
import net.common.ProcessorUtils.MessageGenerators.getGroup
import net.common.ProcessorUtils.MessageGenerators.getGroupList
import net.common.ProcessorUtils.MessageGenerators.getProduct
import net.common.ProcessorUtils.MessageGenerators.getProductList
import net.common.ProcessorUtils.MessageGenerators.increaseProductCount
import net.common.ProcessorUtils.MessageGenerators.removeGroup
import net.common.ProcessorUtils.MessageGenerators.removeProduct
import net.common.ProcessorUtils.MessageGenerators.setProductPrice
import net.common.ProcessorUtils.Messages.byeMessage
import net.common.ProcessorUtils.Messages.groupListMessage
import net.common.ProcessorUtils.Messages.groupMessage
import net.common.ProcessorUtils.Messages.productListMessage
import net.common.ProcessorUtils.Messages.productMessage
import net.common.ProcessorUtils.Messages.successfulDeletionMessage
import net.common.ProcessorUtils.Messages.timeMessage
import net.common.ProcessorUtils.Parser.id
import net.common.ProcessorUtils.Parser.idAndDouble
import net.common.ProcessorUtils.Parser.idAndInt
import net.common.ProcessorUtils.Parser.idAndName
import net.common.ProcessorUtils.Parser.product
import protocol.Message
import protocol.Message.ClientCommands.*
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

        fun getGroupList(message: Message): Message {
            val list = db.getGroupList()
            return groupListMessage(list)
        }
        //TODO add criterions to group and product lists
        fun getProductList(message: Message): Message {
            val list = db.getProductList()
            return productListMessage(list)
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
        fun byeMessage() = Message(SERVER_BYE, msg = "Bye!")
        fun wrongNameFormatMessage() = Message(WRONG_NAME_ERROR, msg = "Name you entered is in wrong format")
        fun nameTakenMessage() = Message(NAME_TAKEN_ERROR, msg = "Name is already taken")
        fun successfulDeletionMessage(id: Int) = Message(SUCCESSFUL_DELETION, msg = "Successfully deleted $id")
        fun nonEmptyProductMessage() = Message(NON_EMPTY_PRODUCT_ERROR, msg = "Can't delete product where quantity != 0")
        fun groupListMessage(list: ArrayList<Group>) = Message(GROUP_LIST, msg = "$list")
        fun productListMessage(list: ArrayList<Product>) = Message(GROUP_LIST, msg = "$list")
    }


    fun catchException(e: Throwable): Message = when (e) {
        is DaoProduct.NoSuchProductIdException -> Messages.noSuchIdMessage()
        is DaoProduct.NotEnoughItemsException -> Messages.notEnoughItemsMessage()
        is DaoProduct.NoSuchGroupIdException -> Messages.noSuchIdMessage()
        is DaoProduct.NameTakenException -> Messages.nameTakenMessage()
        is Parser.ParseException -> Messages.wrongMsgFormatMessage()
        is DaoProduct.WrongNameFormatException -> Messages.wrongNameFormatMessage()
        is IllegalArgumentException -> Messages.wrongMsgFormatMessage()
        is DaoProduct.NonEmptyProductException -> Messages.nonEmptyProductMessage()
        else -> Messages.internalErrorMessage()
    }

    fun chooseResponse(message: Message): Message {
        return when (Message.ClientCommands[message.cType]) {
            GET_PRODUCT ->
                getProduct(message)
            ADD_GROUP ->
                addGroup(message)
            ADD_PRODUCT ->
                addProduct(message)
            INCREASE_PRODUCT_COUNT ->
                increaseProductCount(message)
            DECREASE_PRODUCT_COUNT ->
                decreaseProductCount(message)
            SET_PRODUCT_PRICE ->
                setProductPrice(message)
            BYE ->
                byeMessage()
            GET_TIME ->
                timeMessage()
            GET_GROUP ->
                getGroup(message)
            CHANGE_PRODUCT_NAME ->
                changeProductName(message)
            CHANGE_GROUP_NAME ->
                changeGroupName(message)
            DELETE_PRODUCT ->
                removeProduct(message)
            DELETE_GROUP ->
                removeGroup(message)
            GET_PRODUCT_LIST ->
                getProductList(message)
            GET_GROUP_LIST ->
                getGroupList(message)
        }
    }
}