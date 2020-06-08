import db.DaoProduct
import db.entities.Criterion
import db.entities.Product
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DaoProductTest {

    private val sampleProds = arrayListOf(
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

    private val sampleGroups = arrayListOf(
            "grains",
            "legumes",
            "fruits",
            "vegetables"
    )

    private fun DaoProduct.populate() {
        sampleProds.forEach { insertProduct(it) }
        sampleGroups.forEach { addGroup(it) }
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
        val retrievedList = dao.getProductList()

        sampleProds.forEach {
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
                sampleProds.filter { it.price <= price }.toTypedArray(),
                dao.getProductList(criterion = criterion).toArray()
        )


        price = Math.random() * 10
        criterion = Criterion().lower(price)

        assertArrayEquals(
                sampleProds.filter { it.price >= price }.toTypedArray(),
                dao.getProductList(criterion = criterion).toArray()
        )

        val query = "ea"
        criterion = Criterion().query(query)

        assertArrayEquals(
                sampleProds.filter { query in it.name }.toTypedArray(),
                dao.getProductList(criterion = criterion).toArray()
        )

        dao.deleteAll()

        val prodSlice = sampleProds.slice(1..3)
        val ids = prodSlice.map { dao.insertProduct(it) }.toSet()

        criterion = Criterion().ids(ids)

        assertArrayEquals(
                prodSlice.toTypedArray(),
                dao.getProductList(criterion = criterion).toArray()
        )

        val groupId = dao.addGroup("testGroup")

        val prods = (1..10).map { "prod$it" }.map { Product(it, 0.00) }
        //insert all products and add to group
        prods.map { dao.insertProduct(it) }.forEach { dao.setToGroup(it, groupId) }

        criterion = Criterion().groupId(groupId)

        assertArrayEquals(
                prods.toTypedArray(),
                dao.getProductList(criterion = criterion).toArray()
        )
    }

    @Test
    fun groupExistence() {
        val name = "test"
        val groupId = dao.addGroup(name)

        assertTrue(dao.groupExists(name))

        dao.deleteGroup(groupId)

        assertTrue(!dao.groupExists(groupId))

        assertThrows(DaoProduct.NoSuchGroupIdException::class.java) {
            dao.deleteGroup(groupId)
        }
    }

    @Test
    fun groupList() {
        val groups = dao.getGroupList()

        assertArrayEquals(
                sampleGroups.toArray(),
                groups.map { it.second }.toTypedArray()
        )

        val name = "TEST GROUP"
        dao.addGroup(name)

        assertTrue(dao.getGroupList().map { it.second }.contains(name))


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

        val retrievedList = dao.getProductList(criterion = criterion)


        val filteredSamples = sampleProds.filter { it.price in lower..upper && query in it.name }


        assertArrayEquals(
                retrievedList.toArray(),
                filteredSamples.toTypedArray()
        )
    }

    @Test
    internal fun delete() {
        val name = "sandwich"
        val product = Product(name, 0.0)
        val id = dao.insertProduct(product)

        assertTrue(
                dao.productExists(name)
        )

        dao.deleteProduct(id)

        assertFalse(
                dao.productExists(name)
        )

        assertNull(
                dao.getProduct(id)
        )

        dao.getProductList().forEach {
            dao.deleteProduct(it.name)
        }

        assertTrue(
                dao.getProductList().isEmpty()
        )
    }

    @Test
    fun numberTesting() {
        val name = "buckwheat"
        assertEquals(dao.productAmount(name), 0)

        val increment = 41
        dao.addItems(name, increment)

        assertEquals(dao.productAmount(name), increment)

        val decrement = 13
        dao.removeItems(name, decrement)

        assertEquals(dao.productAmount(name), increment - decrement)

        assertNull(dao.productAmount("non-existent product"))
        assertNull(dao.productAmount(-1))

    }

    @Test
    fun groupTests() {
        assertTrue(dao.groupExists("grains"))

        val name = "test"
        val groupId = dao.addGroup(name)

        assertTrue(dao.groupExists(name))

        val prodId = dao.getProduct("buckwheat")!!.id!!

        dao.setToGroup(prodId, groupId)

        val prod = dao.getProduct(prodId)!!

        assertTrue(prod.groupId == groupId)


    }

    @Test
    fun numberThrows() {
        assertThrows(IllegalArgumentException::class.java) { dao.addItems(0, 0) }
        assertThrows(IllegalArgumentException::class.java) { dao.addItems("", 0) }
        assertThrows(IllegalArgumentException::class.java) { dao.removeItems(0, 0) }
        assertThrows(IllegalArgumentException::class.java) { dao.removeItems("", 0) }
    }

    @Test
    fun get() {
        val name = "sandwich"
        val price = 34.0
        val product = Product(name, price)


        assertNull(dao.getProduct(name))

        val id = dao.insertProduct(product)

        assertNotNull(dao.getProduct(name))
        assertNotNull(dao.getProduct(id))

        dao.deleteProduct(id)

        assertNull(dao.getProduct(name))
        assertNull(dao.getProduct(id))

    }

    @Test
    fun setPrice() {
        val name = "sandwich"
        val price = 34.0
        val newPrice = 41241.0
        dao.insertProduct(Product(name, price))

        var retrieved = dao.getProduct(name) ?: return

        assertEquals(retrieved.price, price)

        dao.setPrice(retrieved.id!!, newPrice)

        retrieved = dao.getProduct(name) ?: return

        assertEquals(retrieved.price, newPrice)


    }

    @Test
    fun isTaken() {
        val name = "testname"

        dao.insertProduct(Product(name, 0.0))
        assertTrue(
                dao.productExists(name)
        )
        assertFalse(
                dao.productExists(name + "12414")
        )
    }

    @Test
    fun deleteAll() {
        assertTrue(dao.getProductList().isNotEmpty())

        dao.deleteAll()
        assertTrue(
                dao.getProductList().isEmpty()
        )
    }
}