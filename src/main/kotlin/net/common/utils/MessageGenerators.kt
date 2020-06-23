package net.common.utils

import net.common.Processor
import net.common.utils.InputParsers.id
import net.common.utils.InputParsers.idAndName
import net.common.utils.InputParsers.idAndNumber
import net.common.utils.InputParsers.idAndPrice
import net.common.utils.InputParsers.name
import net.common.utils.InputParsers.product
import net.common.utils.Messages.deletedGroupMessage
import net.common.utils.Messages.deletedProductMessage
import net.common.utils.Messages.groupListMessage
import net.common.utils.Messages.groupMessage
import net.common.utils.Messages.productListMessage
import net.common.utils.Messages.productMessage
import protocol.Message

object MessageGenerators {
    fun getGroup(message: Message): Message {
        val id = message.msg.id()
        val group = Processor.db.getGroup(id)
        return groupMessage(group)
    }

    fun addProduct(message: Message): Message {
        val prod = message.msg.product()
        val id = Processor.db.insertProduct(prod)
        val product = Processor.db.getProduct(id)
        return productMessage(product)
    }

    fun setProductPrice(message: Message): Message {
        val (id, price) = message.msg.idAndPrice()
        Processor.db.setPrice(id, price)
        val product = Processor.db.getProduct(id)
        return productMessage(product)
    }

    fun decreaseProductCount(message: Message): Message {
        val (id, decrement) = message.msg.idAndNumber()
        Processor.db.removeItems(id, decrement)
        val product = Processor.db.getProduct(id)
        return productMessage(product)
    }

    fun increaseProductCount(message: Message): Message {
        val (id, increment) = message.msg.idAndNumber()
        Processor.db.addItems(id, increment)
        val product = Processor.db.getProduct(id)
        return productMessage(product)
    }

    fun getProduct(message: Message): Message {
        val id = message.msg.id()
        val product = Processor.db.getProduct(id)
        return productMessage(product)
    }

    fun changeProductName(message: Message): Message {
        val (id, name) = message.msg.idAndName()
        Processor.db.changeProductName(id, name)
        val product = Processor.db.getProduct(id)
        return productMessage(product)
    }

    fun changeGroupName(message: Message): Message {
        val (id, name) = message.msg.idAndName()
        Processor.db.updateGroup(id, name)
        val group = Processor.db.getGroup(id)
        return groupMessage(group)
    }

    fun addGroup(message: Message): Message {
        val name = message.msg.name()
        val id = Processor.db.addGroup(name)
        val group = Processor.db.getGroup(id)
        return groupMessage(group)
    }

    fun removeGroup(message: Message): Message {
        val id = message.msg.id()
        Processor.db.deleteGroup(id)
        return deletedGroupMessage(id)
    }

    fun removeProduct(message: Message): Message {
        val id = message.msg.id()
        Processor.db.deleteProduct(id)
        return deletedProductMessage(id)
    }

    fun getGroupList(message: Message): Message {
        val list = Processor.db.getGroupList()
        return groupListMessage(list)
    }

    //TODO add criterions to group and product lists
    fun getProductList(message: Message): Message {
        val list = Processor.db.getProductList()
        return productListMessage(list)
    }

}