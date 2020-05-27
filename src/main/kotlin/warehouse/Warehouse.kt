package warehouse

import java.util.concurrent.ConcurrentHashMap

object Warehouse {
    private val groups = ConcurrentHashMap<String, Group>()
    private val items = ConcurrentHashMap<Item, Int>()


    fun amount(item: Item): Int {
        return items[item] ?: throw NoSuchItemException("No item $item in Warehouse")
    }

    fun remove(item: Item, number: Int = 1) {
        if (number <= 0)
            throw IllegalArgumentException("Number can't be zero or less")

        val amount = items[item] ?: throw NoSuchItemException("No item $item in warehouse")

        if (amount < number)
            throw IllegalArgumentException("There isn't enough items in warehouse to remove")

        items[item] = amount - number
    }

    fun add(item: Item, number: Int = 1) {
        if (number <= 0)
            throw IllegalArgumentException("Number can't be zero or less")

        val amount = items[item] ?: throw NoSuchItemException("No item $item in warehouse")

        items[item] = amount + number
    }

    fun createGroup(name: String) {
        if( groups.contains(name) )
            throw java.lang.IllegalArgumentException("Such group already exists")

        if(name == "")
            throw java.lang.IllegalArgumentException("Can't have an empty name")

        groups[name] = Group(name)
    }

    fun addItemToGroup(item: Item, group: Group): Nothing = TODO()




}

