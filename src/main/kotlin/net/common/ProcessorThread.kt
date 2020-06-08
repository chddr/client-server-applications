package net.common

import db.DaoProduct.*
import net.common.ProcessorUtils.MessageGenerators.addProduct
import net.common.ProcessorUtils.MessageGenerators.changeGroupName
import net.common.ProcessorUtils.MessageGenerators.changeProductName
import net.common.ProcessorUtils.MessageGenerators.decreaseProductCount
import net.common.ProcessorUtils.MessageGenerators.getGroupName
import net.common.ProcessorUtils.MessageGenerators.getProduct
import net.common.ProcessorUtils.MessageGenerators.increaseProductCount
import net.common.ProcessorUtils.MessageGenerators.setProductPrice
import net.common.ProcessorUtils.Messages.byeMessage
import net.common.ProcessorUtils.Messages.internalErrorMessage
import net.common.ProcessorUtils.Messages.nameTakenMessage
import net.common.ProcessorUtils.Messages.noSuchIdMessage
import net.common.ProcessorUtils.Messages.notEnoughItemsMessage
import net.common.ProcessorUtils.Messages.timeMessage
import net.common.ProcessorUtils.Messages.wrongCommand
import net.common.ProcessorUtils.Messages.wrongMsgFormatMessage
import net.common.ProcessorUtils.Parser.ParseException
import protocol.Message
import protocol.Message.ClientCommands
import protocol.Message.ClientCommands.*

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
            } catch (e: Throwable) {
                catchException(e)
            }
        else wrongCommand()
    }

    private fun catchException(e: Throwable): Message = when (e) {
        is NoSuchProductIdException -> noSuchIdMessage()
        is NotEnoughItemsException -> notEnoughItemsMessage()
        is NoSuchGroupIdException -> noSuchIdMessage()
        is NameTakenException -> nameTakenMessage()
        is ParseException -> wrongMsgFormatMessage()
        else -> internalErrorMessage()
    }

    private fun chooseResponse(): Message {
        return when (ClientCommands[message.cType]) {
            GET_PRODUCT ->
                getProduct(message)
            ADD_GROUP ->
                internalErrorMessage() //TODO
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
            GET_GROUP_NAME ->
                getGroupName(message)
            CHANGE_PRODUCT_NAME ->
                changeProductName(message)
            CHANGE_GROUP_NAME ->
                changeGroupName(message)
        }
    }

}
