package net.common.utils

import db.DaoProduct.*
import net.common.utils.InputParsers.ParseException
import net.common.utils.MessageGenerators.addGroup
import net.common.utils.MessageGenerators.addProduct
import net.common.utils.MessageGenerators.changeGroupName
import net.common.utils.MessageGenerators.changeProductName
import net.common.utils.MessageGenerators.decreaseProductCount
import net.common.utils.MessageGenerators.getGroup
import net.common.utils.MessageGenerators.getGroupList
import net.common.utils.MessageGenerators.getProduct
import net.common.utils.MessageGenerators.getProductList
import net.common.utils.MessageGenerators.increaseProductCount
import net.common.utils.MessageGenerators.removeGroup
import net.common.utils.MessageGenerators.removeProduct
import net.common.utils.MessageGenerators.setProductPrice
import net.common.utils.Messages.byeMessage
import net.common.utils.Messages.internalException
import net.common.utils.Messages.nameTakenException
import net.common.utils.Messages.noSuchIdException
import net.common.utils.Messages.nonEmptyProductException
import net.common.utils.Messages.notEnoughItemsExceptions
import net.common.utils.Messages.timeMessage
import net.common.utils.Messages.wrongCommandException
import net.common.utils.Messages.wrongFormatException
import net.common.utils.Messages.wrongNameFormatException
import protocol.Message
import protocol.Message.ClientCommands.*
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
        is ParseException, is IllegalArgumentException -> wrongFormatException()
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