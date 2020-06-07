
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import db.DaoProduct
import db.entities.Criterion
import db.entities.Product

internal class DaoProductTest {

    private val sampleList = arrayListOf(
            Product("wheat", 4.0),
            Product("banana", 3.0),
            Product("kale", 5.0),
            Product("buckwheat", 1.9),
            Product("rice", 7.4),
            Product("cabbage", 1.5),
            Product("tofu", 9.9),
            Product("quinoa", 0.5),
            Product("yeast", 6.0)
    )

    private fun DaoProduct.populate() {
        sampleList.forEach { insert(it) }
    }

    private val dao = DaoProduct("file.db:memory")

    @BeforeEach
    fun setup() {
        dao.populate()
    }

    @AfterEach
    fun tearDown() {
        dao.deleteAll()
    }

    @Test
    fun getList() {
        val retrievedList = dao.getList()

        sampleList.forEach {
            assertTrue(
                    dao.productExists(it.name)
            )
            assertTrue(
                    retrievedList.contains(it)
            )
        }
    }

    @Test
    fun getListWithCriterions() {

        var price = Math.random() * 10
        var criterion = Criterion().upper(price)

        assertArrayEquals(
                sampleList.filter { it.price <= price }.toTypedArray(),
                dao.getList(criterion = criterion).toArray()
        )


        price = Math.random() * 10
        criterion = Criterion().lower(price)

        assertArrayEquals(
                sampleList.filter { it.price >= price }.toTypedArray(),
                dao.getList(criterion = criterion).toArray()
        )

        val query = "ea"
        criterion = Criterion().query(query)

        assertArrayEquals(
                sampleList.filter { query in it.name }.toTypedArray(),
                dao.getList(criterion = criterion).toArray()
        )

        dao.deleteAll()

        val prodSlice = sampleList.slice(1..3)
        val ids = prodSlice.map { dao.insert(it) }.toSet()

        criterion = Criterion().ids(ids)

        assertArrayEquals(
                prodSlice.toTypedArray(),
                dao.getList(criterion = criterion).toArray()
        )
    }

    @Test
    fun getListWithMultipleCriterions() {

        val lower = 2.5
        val upper = 7.5
        val query = "ea"

        val criterion = Criterion()
                .lower(lower)
                .upper(upper)
                .query(query)

        val retrievedList = dao.getList(criterion = criterion)

        println(retrievedList)

        val filteredSamples = sampleList.filter { it.price in lower..upper && query in it.name }

        println(filteredSamples)

        assertArrayEquals(
                retrievedList.toArray(),
                filteredSamples.toTypedArray()
        )


    }

    @Test
    internal fun delete() {
        val name = "sandwich"
        val product = Product(name, 0.0)
        val id = dao.insert(product)

        assertTrue(
                dao.productExists(name)
        )

        dao.delete(id)

        assertFalse(
                dao.productExists(name)
        )

        assertNull(
                dao.get(id)
        )

        dao.getList().forEach {
            dao.delete(it.name)
        }

        assertTrue(
                dao.getList().isEmpty()
        )
    }

    @Test
    fun numberTesting() {
        val name = "buckwheat"
        assertEquals(dao.amount(name), 0)

        val increment = 41
        dao.addItems(name, increment)

        assertEquals(dao.amount(name), increment)

        val decrement = 13
        dao.removeItems(name, decrement)

        assertEquals(dao.amount(name), increment - decrement)

        assertNull(dao.amount("non-existent product"))
        assertNull(dao.amount(-1))

    }

    @Test
    fun numberThrows() {
        assertThrows(IllegalArgumentException::class.java) {dao.addItems(0,0)}
        assertThrows(IllegalArgumentException::class.java) {dao.addItems("",0)}
        assertThrows(IllegalArgumentException::class.java) {dao.removeItems(0,0)}
        assertThrows(IllegalArgumentException::class.java) {dao.removeItems("",0)}
    }

    @Test
    fun get() {
        val name = "sandwich"
        val price = 34.0
        val product = Product(name, price)


        assertNull(dao.get(name))

        val id = dao.insert(product)

        assertNotNull(dao.get(name))
        assertNotNull(dao.get(id))

        dao.delete(id)

        assertNull(dao.get(name))
        assertNull(dao.get(id))

    }

    @Test
    fun setPrice() {
        val name = "sandwich"
        val price = 34.0
        val newPrice = 41241.0
        dao.insert(Product(name, price))

        var retrieved = dao.get(name) ?: return

        assertEquals(retrieved.price, price)

        dao.setPrice(retrieved.id!!, newPrice)

        retrieved = dao.get(name) ?: return

        assertEquals(retrieved.price, newPrice)


    }

    @Test
    fun isTaken() {
        val name = "testname"

        dao.insert(Product(name, 0.0))
        assertTrue(
                dao.productExists(name)
        )
        assertFalse(
                dao.productExists(name + "12414")
        )
    }

    @Test
    fun deleteAll() {
        assertTrue(dao.getList().isNotEmpty())

        dao.deleteAll()
        assertTrue(
                dao.getList().isEmpty()
        )
    }
}