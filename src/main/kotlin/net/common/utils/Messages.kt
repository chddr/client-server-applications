package net.common.utils

import db.entities.Group
import db.entities.Product
import protocol.Message
import java.time.LocalTime

object Messages {
    fun timeMessage() = Message(Message.ServerCommands.SERVER_TIME, msg = "Time is: ${LocalTime.now()}")
    fun productMessage(product: Product) = Message(Message.ServerCommands.PRODUCT, msg = "$product")
    fun groupMessage(group: Group): Message = Message(Message.ServerCommands.GROUP, msg = "$group")
    fun byeMessage() = Message(Message.ServerCommands.SERVER_BYE, msg = "Bye!")
    fun successfulDeletionMessage(id: Int) = Message(Message.ServerCommands.SUCCESSFUL_DELETION, msg = "Successfully deleted $id")
    fun groupListMessage(list: ArrayList<Group>) = Message(Message.ServerCommands.GROUP_LIST, msg = "$list")
    fun productListMessage(list: ArrayList<Product>) = Message(Message.ServerCommands.GROUP_LIST, msg = "$list")

    fun wrongCommandException() = Message(Message.ServerCommands.WRONG_COMMAND_ERROR, msg = "")
    fun internalException() = Message(Message.ServerCommands.INTERNAL_ERROR, msg = "Report to the dev!")
    fun nonEmptyProductException() = Message(Message.ServerCommands.NON_EMPTY_PRODUCT_ERROR, msg = "Can't delete product where quantity != 0")
    fun wrongNameFormatException() = Message(Message.ServerCommands.WRONG_NAME_ERROR, msg = "Name you entered is in wrong format")
    fun nameTakenException() = Message(Message.ServerCommands.NAME_TAKEN_ERROR, msg = "Name is already taken")
    fun notEnoughItemsExceptions() = Message(Message.ServerCommands.NOT_ENOUGH_ITEMS_ERROR, msg = "Not enough items to remove")
    fun wrongFormatException() = Message(Message.ServerCommands.WRONG_MESSAGE_FORMAT_ERROR, msg = "Wrong message format")
    fun noSuchIdException() = Message(Message.ServerCommands.NO_SUCH_ID_ERROR, msg = "No such ID in table")
}