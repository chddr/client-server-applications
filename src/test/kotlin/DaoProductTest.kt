import db.DaoProduct
import db.entities.Criterion
import db.entities.Product
import db.exceptions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DaoProductTest {

    companion object {

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

        fun DaoProduct.populate() {
            sampleProds.forEach { insertProduct(it) }
            sampleGroups.forEach { addGroup(it) }
        }
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

        val prods = (1..10).map { "prod$it" }.map { Product(it, 0.01) }
        //insert all products and add to group
        prods.map { dao.insertProduct(it) }.forEach { dao.setToGroup(it, groupId) }

        criterion = Criterion().groupId(groupId)

        assertArrayEquals(
                prods.toTypedArray(),
                dao.getProductList(criterion = criterion).toArray()
        )
    }

    @Test
    fun totalSumTest() {
        val amounts = sampleProds
                .map { (Math.random() * 10).toInt() + 1 }

        val checkSum = (sampleProds zip amounts).map { (prod, amount) -> prod.price * amount }.sum()

        amounts
                .forEachIndexed { index, amount -> dao.addItems(dao.getProduct(sampleProds[index].name).id!!, amount) }


        val sum = dao.totalSum()

        assertEquals(checkSum, sum)
    }

    @Test
    fun groupExistence() {
        val name = "test"
        val groupId = dao.addGroup(name)

        assertTrue(dao.groupExists(name))

        dao.deleteGroup(groupId)

        assertTrue(!dao.groupExists(groupId))

        assertThrows(NoSuchGroupIdException::class.java) {
            dao.deleteGroup(groupId)
        }
    }

    @Test
    fun groupList() {
        val groups = dao.getGroupList()

        assertArrayEquals(
                sampleGroups.toArray(),
                groups.map { it.name }.toTypedArray()
        )

        val name = "TEST GROUP"
        dao.addGroup(name)

        assertTrue(dao.getGroupList().map { it.name }.contains(name))


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
    internal fun deleteProduct() {
        val name = "sandwich"
        val product = Product(name, 0.1)
        val id = dao.insertProduct(product)

        assertTrue(
                dao.productExists(name)
        )

        dao.deleteProduct(id)

        assertFalse(
                dao.productExists(name)
        )

        assertThrows(NoSuchProductIdException::class.java)
        { dao.getProduct(id) }

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

    }

    @Test
    fun groupTests() {
        assertTrue(dao.groupExists("grains"))

        val name = "test"
        val groupId = dao.addGroup(name)

        assertTrue(dao.groupExists(name))

        val prodId = dao.getProduct("buckwheat").id!!

        dao.setToGroup(prodId, groupId)

        val prod = dao.getProduct(prodId)

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


        assertThrows(NoSuchProductIdException::class.java) {
            dao.getProduct(name)
        }

        val id = dao.insertProduct(product)

        assertNotNull(dao.getProduct(name))
        assertNotNull(dao.getProduct(id))

        dao.deleteProduct(id)

        assertThrows(NoSuchProductIdException::class.java) {
            dao.getProduct(name)
        }
        assertThrows(NoSuchProductIdException::class.java) {
            dao.getProduct(id)
        }

    }

    @Test
    fun setPrice() {
        val name = "sandwich"
        val price = 34.0
        val newPrice = 41241.0


        dao.insertProduct(Product(name, price))

        var retrieved = dao.getProduct(name)

        assertEquals(retrieved.price, price)

        dao.setPrice(retrieved.id!!, newPrice)

        assertThrows(WrongPriceException::class.java)
        { dao.setPrice(retrieved.id!!, -0.412) }

        retrieved = dao.getProduct(name)

        assertEquals(retrieved.price, newPrice)


    }

    @Test
    fun groupNameChange() {
        val newName = "TEST"

        val id = dao.addGroup("wrong")

        dao.changeGroupName(id, newName)

        assertEquals(
                newName,
                dao.getGroup(id).name
        )

    }

    @Test
    fun productNameChange() {
        val newName = "TEST"

        val id = dao.insertProduct(Product("test", 0.42))

        dao.changeProductName(id, newName)

        assertEquals(
                newName,
                dao.getProduct(id).name
        )

    }

    @Test
    fun isTaken() {
        val name = "testname"

        dao.insertProduct(Product(name, 0.1))
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

    @Test
    fun exceptionTesting() {
        assertThrows(NameTakenException::class.java) {
            dao.insertProduct(Product("buckwheat", 0.4))
        }

        assertThrows(NameTakenException::class.java) {
            dao.addGroup("grains")
        }

        assertThrows(NoSuchGroupIdException::class.java) {
            dao.deleteGroup(1048120213)
        }

        assertThrows(NoSuchGroupIdException::class.java) {
            dao.setToGroup(1048120213, 3412414)
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.getGroupList(-1)
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.getGroupList(1, 0)
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.getProductList(-1)
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.getProductList(1, 0)
        }

        assertThrows(NoSuchProductIdException::class.java) {
            dao.changeProductName(1048120213, "totally new free name")
        }

        assertThrows(NameTakenException::class.java) {
            dao.changeProductName(dao.insertProduct(Product("another unique name", 4124.4)), "buckwheat")
        }

        assertThrows(NoSuchGroupIdException::class.java) {
            dao.getGroup(165215)
        }

        assertThrows(NoSuchGroupIdException::class.java) {
            dao.changeGroupName(165215412, "testsing")
        }

        assertThrows(NameTakenException::class.java) {
            dao.changeGroupName(dao.addGroup("new  naem t"), "grains")
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.addItems(dao.insertProduct(Product("yo yankee with  brim", 4.4)), -4)
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.addItems("buckwheat", -4)
        }

        assertThrows(NoSuchProductIdException::class.java) {
            dao.addItems("buckwheeeeeeeeeeeeeeat", 10)
        }

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            dao.removeItems("buckwheat", -4)
        }

        assertThrows(NoSuchProductIdException::class.java) {
            dao.removeItems("buckwheeeeeeeeeeeeeeat", 10)
        }

        assertThrows(NotEnoughItemsException::class.java) {
            dao.removeItems(dao.insertProduct(Product("yo brim with no yank", 4.4)), 100009999)
        }

        assertThrows(NoSuchProductIdException::class.java) {
            dao.productAmount("091549012840912840912")
        }

        assertThrows(NoSuchProductIdException::class.java) {
            dao.productAmount(204910249)
        }


    }
}