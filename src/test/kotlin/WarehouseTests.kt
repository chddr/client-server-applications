
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import warehouse.Item
import warehouse.Warehouse
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WarehouseTests {

    @AfterEach
    fun reset() = Warehouse.reset()

    @Test
    fun nonExistentItemOperations() {
        Assertions.assertNull(Warehouse.removeItem(0, 0, 1))
        Assertions.assertNull(Warehouse.getItemCount(0, 0))
        Assertions.assertNull(Warehouse.setPrice(0, 0, 1.1))
        Assertions.assertNull(Warehouse.addItem(0, 0, 1))
        Assertions.assertNull(Warehouse.addItemToGroup(0, Item("Buckwheat", 3.3, 5)))
    }

    @Test
    fun trivialArithmetics() {
        Warehouse.addGroup("Legumes")

        Warehouse.addItemToGroup(0, Item("Kidney beans", 5.5, 5))
        Assertions.assertEquals(Warehouse.getItemCount(0, 0), 5);

        Warehouse.addItem(0, 0, 3)
        Assertions.assertEquals(Warehouse.getItemCount(0, 0), 5 + 3)

        Warehouse.removeItem(0, 0, 4)
        Assertions.assertEquals(Warehouse.getItemCount(0, 0), 5 + 3 - 4)
    }

    @Test
    fun concurrentArithmetics() {
        Warehouse.addGroup("Legumes")

        Warehouse.addItemToGroup(0, Item("Kidney beans", 5.5, 0))
        Assertions.assertEquals(Warehouse.getItemCount(0, 0), 0);

        val executors = Executors.newFixedThreadPool(1000)
        repeat(1000) {
            executors.submit(
                    Thread {
                        Warehouse.addItem(0,0,1)
                    }
            )
        }

        try {
            executors.shutdown()
            while (!executors.awaitTermination(10, TimeUnit.SECONDS)) {
                println("waiting for messages to be sent")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }


        Assertions.assertEquals(Warehouse.getItemCount(0, 0), 1000)

    }


}