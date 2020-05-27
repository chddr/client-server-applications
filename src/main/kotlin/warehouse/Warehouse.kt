package warehouse

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


object Warehouse {
    private val groups = ConcurrentHashMap<Int, Group>()

    private val groupCounter = AtomicInteger(0)

    fun reset() {
        groups.clear()
        groupCounter.set(0)
    }

    fun getGroup(groupId: Int) = groups[groupId]
    fun getProduct(groupId: Int, itemId: Int) = groups[groupId]?.get(itemId)
    fun getGroups() = Collections.unmodifiableMap(groups)

    fun addGroup(nameOfGroup: String) = groups.put(groupCounter.getAndIncrement(), Group(nameOfGroup))

    fun getItemCount(groupId: Int, itemId: Int): Int? {
        synchronized(groups) {
            val group = groups[groupId] ?: return null
            val product = group[itemId] ?: return null
            return product.count
        }
    }


    /**returns current product count or null if operation failed*/
    fun removeItem(groupId: Int, itemId: Int, number: Int): Int? {
        if (number <= 0) throw IllegalArgumentException("Must be positive")

        synchronized(groups) {
            val group = groups[groupId] ?: return null
            val product = group[itemId] ?: return null

            if (product.count < number) throw IllegalArgumentException("Not enough items to remove from the warehouse")
            product.count -= number

            return product.count
        }
    }

    /**returns current product count or null if operation failed*/
    fun addItem(groupId: Int, itemId: Int, number: Int): Int? {
        if (number <= 0) throw IllegalArgumentException("Must be positive")

        synchronized(groups) {
            val group = groups[groupId] ?: return null
            val product = group[itemId] ?: return null

            product.count += number
            return product.count
        }
    }

    fun addItemToGroup(groupId: Int, item: Item): Int? {
        synchronized(groups) {
            val group = groups[groupId] ?: return null
            return group.addItem(item)
        }
    }

    fun setPrice(groupId: Int, itemId: Int, price: Double): Double? {
        if (price <= 0) throw IllegalArgumentException("Must be positive")

        synchronized(groups) {
            val group = groups[groupId] ?: return null
            val product = group[itemId] ?: return null

            product.price = price
            return price
        }
    }


}

