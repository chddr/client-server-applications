package net.common.utils

import db.entities.Group
import db.entities.Group.Companion.toJsonString
import db.entities.Product
import db.entities.Product.Companion.toJsonString
import protocol.Message
import protocol.Message.ServerCommands.*
import java.time.LocalTime


object Messages {
    fun timeMessage() = Message(SERVER_TIME, msg = "Time is: {time:'${LocalTime.now()}'}")
    fun productMessage(product: Product) = Message(PRODUCT, msg = "$product")
    fun groupMessage(group: Group): Message = Message(GROUP, msg = "$group")
    fun byeMessage() = Message(SERVER_BYE, msg = "Bye!")
    fun deletedProductMessage(id: Int) = Message(DELETED_PRODUCT, msg = "{id:$id}")
    fun deletedGroupMessage(id: Int) = Message(DELETED_GROUP, msg = "{id:$id}")
    fun groupListMessage(list: ArrayList<Group>) = Message(GROUP_LIST, msg = list.toJsonString())
    fun productListMessage(list: ArrayList<Product>) = Message(PRODUCT_LIST, msg = list.toJsonString())

    fun wrongCommandException() = Message(WRONG_COMMAND_ERROR, msg = "")
    fun internalException() = Message(INTERNAL_ERROR, msg = "Report to the dev!")
    fun nonEmptyProductException() = Message(NON_EMPTY_PRODUCT_ERROR, msg = "Can't delete product where quantity != 0")
    fun wrongNameFormatException() = Message(WRONG_NAME_ERROR, msg = "Name you entered is in wrong format")
    fun nameTakenException() = Message(NAME_TAKEN_ERROR, msg = "Name is already taken")
    fun notEnoughItemsExceptions() = Message(NOT_ENOUGH_ITEMS_ERROR, msg = "Not enough items to remove")
    fun wrongFormatException() = Message(WRONG_MESSAGE_FORMAT_ERROR, msg = "Wrong message format")
    fun noSuchIdException() = Message(NO_SUCH_ID_ERROR, msg = "No such ID in table")
}