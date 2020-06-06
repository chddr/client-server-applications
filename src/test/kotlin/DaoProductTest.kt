
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pr4.DaoProduct
import pr4.entities.Product

internal class DaoProductTest {

    private val dao = DaoProduct("file.db:memory")

    @AfterEach
    fun tearDown() {
        dao.deleteAll()
    }

    @Test
    fun getList() {
        val products = (0..9).map { Product("prod$it", it.toDouble()) }

        products.forEach { dao.insert(it) }

        val retrievedList = dao.getList()

        products.forEach {
            Assertions.assertTrue(
                    dao.isTaken(it.name)
            )
            Assertions.assertTrue(
                    retrievedList.contains(it)
            )
        }
    }

    @Test
    internal fun delete() {
        val name = "buckwheat"
        val product = Product(name, 0.0)
        val id = dao.insert(product)

        Assertions.assertTrue(
                dao.isTaken(name)
        )

        dao.delete(id)

        Assertions.assertFalse(
                dao.isTaken(name)
        )

        Assertions.assertNull(
                dao.get(id)
        )

        dao.populate()

        dao.getList().forEach {
            dao.delete(it.name)
        }

        Assertions.assertTrue(
                dao.getList().isEmpty()
        )
    }

    @Test
    fun get() {
        val name = "buckwheat"
        val price = 34.0
        val product = Product(name, price)


        Assertions.assertNull(dao.get(name))

        val id = dao.insert(product)

        Assertions.assertNotNull(dao.get(name))
        Assertions.assertNotNull(dao.get(id))

        dao.delete(id)

        Assertions.assertNull(dao.get(name))
        Assertions.assertNull(dao.get(id))

    }

    @Test
    fun setPrice() {
        val name = "buckwheat"
        val price = 34.0
        val newPrice = 41241.0
        dao.insert(Product(name, price))

        var retrieved = dao.get(name) ?: return

        Assertions.assertEquals(retrieved.price, price)

        dao.setPrice(retrieved.id!!, newPrice)

        retrieved = dao.get(name) ?: return

        Assertions.assertEquals(retrieved.price, newPrice)


    }

    @Test
    fun isTaken() {
        val name = "testname"

        dao.insert(Product(name, 0.0))
        Assertions.assertTrue(
                dao.isTaken(name)
        )
        Assertions.assertFalse(
                dao.isTaken(name + "12414")
        )
    }

    @Test
    fun deleteAll() {
        dao.populate()
        dao.deleteAll()
        Assertions.assertTrue(
                dao.getList().isEmpty()
        )
    }
}