package warehouse

import java.util.concurrent.atomic.AtomicInteger

class Group(var name: String) {
    private val items = HashMap<Int, Item>()
    private val idCounter = AtomicInteger(0)

    fun addItem(item: Item): Int {
        val id = idCounter.getAndIncrement()
        items[id] = item
        return id
    }

    operator fun get(itemId: Int): Item? {
        return items[itemId]
    }


}