package warehouse

import java.util.concurrent.atomic.AtomicInteger

class Group(var name: String) {
    private val items = HashMap<Int, Item>()
    private val idCounter = AtomicInteger(0)

    fun addItem(item: Item) {
        items[idCounter.getAndIncrement()] = item;
    }

    fun productCount() = items.values.sumBy(Item::amount)





}