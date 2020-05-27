package warehouse

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


object Warehouse {
    private val groups = ConcurrentHashMap<Int, Group>()
    private val idCounter = AtomicInteger(0)

    fun addGroup(nameOfGroup: String) = groups.put(idCounter.getAndIncrement(), Group(nameOfGroup))

    fun productCount() = groups.values.sumBy(Group::productCount)

}

