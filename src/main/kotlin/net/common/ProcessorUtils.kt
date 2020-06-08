package net.common

import db.DaoProduct.*
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
import net.common.ProcessorUtils.Messages.internalException
import net.common.ProcessorUtils.Messages.nameTakenException
import net.common.ProcessorUtils.Messages.noSuchIdException
import net.common.ProcessorUtils.Messages.nonEmptyProductException
import net.common.ProcessorUtils.Messages.notEnoughItemsExceptions
import net.common.ProcessorUtils.Messages.productListMessage
import net.common.ProcessorUtils.Messages.productMessage
import net.common.ProcessorUtils.Messages.successfulDeletionMessage
import net.common.ProcessorUtils.Messages.timeMessage
import net.common.ProcessorUtils.Messages.wrongCommandException
import net.common.ProcessorUtils.Messages.wrongFormatException
import net.common.ProcessorUtils.Messages.wrongNameFormatException
import net.common.ProcessorUtils.Parser.id
import net.common.ProcessorUtils.Parser.idAndName
import net.common.ProcessorUtils.Parser.idAndNumber
import net.common.ProcessorUtils.Parser.idAndPrice
import net.common.ProcessorUtils.Parser.product
import org.json.JSONException
import org.json.JSONObject
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
            val (id, price) = message.msg.idAndPrice()
            db.setPrice(id, price)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun decreaseProductCount(message: Message): Message {
            val (id, decrement) = message.msg.idAndNumber()
            db.removeItems(id, decrement)
            val product = db.getProduct(id)
            return productMessage(product)
        }

        fun increaseProductCount(message: Message): Message {
            val (id, increment) = message.msg.idAndNumber()
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
                JSONObject(this).getInt("id")
            } catch (e: JSONException) {
                throw ParseException(e)
            }
        }

        fun String.idAndName(): Pair<Int, String> {
            return try {
                JSONObject(this).run {
                    getInt("id") to getString("name")
                }
            } catch (e: JSONException) {
                throw ParseException(e)
            }
        }

        fun String.idAndNumber(): Pair<Int, Int> {
            return try {
                JSONObject(this).run {
                    getInt("id") to getInt("number")
                }
            } catch (e: JSONException) {
                throw ParseException(e)
            }
        }

        fun String.idAndPrice(): Pair<Int, Double> {
            return try {
                JSONObject(this).run {
                    getInt("id") to getDouble("price")
                }
            } catch (e: JSONException) {
                throw ParseException(e)
            }
        }

        fun String.product(): Product {
            return try {
                JSONObject(this).run {
                    Product(getString("name"), getDouble("price"))
                }
            } catch (e: JSONException) {
                throw ParseException(e)
            }
        }
    }

    object Messages {
        fun timeMessage() = Message(SERVER_TIME, msg = "Time is: ${LocalTime.now()}")
        fun productMessage(product: Product) = Message(PRODUCT, msg = "$product")
        fun groupMessage(group: Group): Message = Message(GROUP, msg = "$group")
        fun byeMessage() = Message(SERVER_BYE, msg = "Bye!")
        fun successfulDeletionMessage(id: Int) = Message(SUCCESSFUL_DELETION, msg = "Successfully deleted $id")
        fun groupListMessage(list: ArrayList<Group>) = Message(GROUP_LIST, msg = "$list")
        fun productListMessage(list: ArrayList<Product>) = Message(GROUP_LIST, msg = "$list")

        fun wrongCommandException() = Message(WRONG_COMMAND_ERROR, msg = "")
        fun internalException() = Message(INTERNAL_ERROR, msg = "Report to the dev!")
        fun nonEmptyProductException() = Message(NON_EMPTY_PRODUCT_ERROR, msg = "Can't delete product where quantity != 0")
        fun wrongNameFormatException() = Message(WRONG_NAME_ERROR, msg = "Name you entered is in wrong format")
        fun nameTakenException() = Message(NAME_TAKEN_ERROR, msg = "Name is already taken")
        fun notEnoughItemsExceptions() = Message(NOT_ENOUGH_ITEMS_ERROR, msg = "Not enough items to remove")
        fun wrongFormatException() = Message(WRONG_MESSAGE_FORMAT_ERROR, msg = "Wrong message format")
        fun noSuchIdException() = Message(NO_SUCH_ID_ERROR, msg = "No such ID in table")

    }

    fun processMessage(message: Message): Message {
        return if (message.cType in Message.ClientCommands)
            try {
                chooseOption(message)
            } catch (e: Throwable) {
                catchException(e)
            }
        else wrongCommandException()
    }


    private fun catchException(e: Throwable): Message = when (e) {
        is NoSuchProductIdException, is NoSuchGroupIdException -> noSuchIdException()
        is Parser.ParseException, is java.lang.IllegalArgumentException -> wrongFormatException()
        is NotEnoughItemsException -> notEnoughItemsExceptions()
        is NameTakenException -> nameTakenException()
        is WrongNameFormatException -> wrongNameFormatException()
        is NonEmptyProductException -> nonEmptyProductException()
        else -> internalException()
    }

    private fun chooseOption(message: Message): Message {
        return when (Message.ClientCommands[message.cType]) {
            GET_PRODUCT -> getProduct(message)
            ADD_GROUP -> addGroup(message)
            ADD_PRODUCT -> addProduct(message)
            INCREASE_PRODUCT_COUNT -> increaseProductCount(message)
            DECREASE_PRODUCT_COUNT -> decreaseProductCount(message)
            SET_PRODUCT_PRICE -> setProductPrice(message)
            BYE -> byeMessage()
            GET_TIME -> timeMessage()
            GET_GROUP -> getGroup(message)
            CHANGE_PRODUCT_NAME -> changeProductName(message)
            CHANGE_GROUP_NAME -> changeGroupName(message)
            DELETE_PRODUCT -> removeProduct(message)
            DELETE_GROUP -> removeGroup(message)
            GET_PRODUCT_LIST -> getProductList(message)
            GET_GROUP_LIST -> getGroupList(message)
        }
    }
}