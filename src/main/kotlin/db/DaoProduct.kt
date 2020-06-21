package db

import db.DBUtils.checkName
import db.DBUtils.extractGroup
import db.DBUtils.extractProduct
import db.DBUtils.generateWhereClause
import db.entities.Criterion
import db.entities.Group
import db.entities.Product
import db.exceptions.*
import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager

class DaoProduct(db: String) : Closeable {

    private val conn: Connection = DriverManager.getConnection("jdbc:sqlite:$db")

    init {
        conn.createStatement().use {
            it.execute("CREATE TABLE IF NOT EXISTS 'groups' ('id' INTEGER PRIMARY KEY AUTOINCREMENT , 'name' TEXT NOT NULL UNIQUE)")
            it.execute("CREATE TABLE IF NOT EXISTS 'products' ('id' INTEGER PRIMARY KEY AUTOINCREMENT , 'name' TEXT NOT NULL UNIQUE , 'price' REAL NOT NULL, 'quantity' INTEGER NOT NULL DEFAULT 0, 'groupId' INTEGER DEFAULT NULL, FOREIGN KEY (groupId) REFERENCES groups(id) ON DELETE SET NULL )")
        }
    }

    /**
     * Inserts a product and returns it's id in the table if everything went ok, otherwise null
     */
    fun insertProduct(product: Product): Int {
        if (!product.price.isFinite() || product.price <= 0) throw WrongPriceException()
        if (productExists(product.name)) throw NameTakenException()
        if (!checkName(product.name)) throw WrongNameFormatException()

        return conn.prepareStatement("INSERT INTO products('name', 'price') VALUES (?,?)").use {
            it.run {
                setString(1, product.name)
                setDouble(2, product.price)

                executeUpdate()
                generatedKeys
            }.getInt("last_insert_rowid()")
        }
    }

    fun addGroup(name: String): Int {
        if (groupExists(name)) throw NameTakenException()
        if (!checkName(name)) throw WrongNameFormatException()

        return conn.prepareStatement("INSERT INTO groups('name') VALUES (?)").use {
            it.run {
                setString(1, name)

                executeUpdate()
                generatedKeys
            }.getInt("last_insert_rowid()")
        }
    }

    fun deleteGroup(id: Int) {
        if (!groupExists(id)) throw NoSuchGroupIdException()

        conn.createStatement().use {
            it.execute("DELETE FROM products WHERE groupId = $id")
            it.execute("DELETE FROM groups WHERE id = $id")
        }
    }

    fun setToGroup(prodId: Int, groupId: Int) {
        if (!groupExists(groupId)) throw NoSuchGroupIdException()
        if (!productExists(prodId)) throw NoSuchProductIdException()

        conn.createStatement().use {
            it.execute("UPDATE products SET groupId = $groupId WHERE id = $prodId")
        }
    }

    fun getProductList(page: Int = 0, size: Int = 20, criterion: Criterion? = null): ArrayList<Product> {
        if (page < 0 || size <= 0) throw IllegalArgumentException("wrong parameters")

        return conn.createStatement().use {
            val conditions = criterion?.let { it1 -> generateWhereClause(it1) } ?: ""
            val query = "SELECT * FROM products $conditions LIMIT $size OFFSET ${page * size}"

            it.executeQuery(query).run {
                ArrayList<Product>().also { prods ->
                    while (next())
                        prods.add(extractProduct())
                }
            }
        }
    }

    fun getGroupList(page: Int = 0, size: Int = 20, query: String? = null): ArrayList<Group> {
        if (page < 0 || size <= 0) throw IllegalArgumentException("wrong parameters")

        return conn.createStatement().use {
            val condition = if (query != null) "WHERE name LIKE '%$query%'" else ""
            val createdQuery = "SELECT * FROM groups $condition LIMIT $size OFFSET ${page * size}"


            it.executeQuery(createdQuery).run {
                ArrayList<Group>().also { prods ->
                    while (next())
                        prods.add(extractGroup())
                }
            }
        }
    }

    fun getProduct(id: Int): Product {
        if (!productExists(id)) throw NoSuchProductIdException()

        return conn.createStatement().use {
            val res = it.executeQuery("SELECT * FROM products WHERE id = $id")

            res.next()
            res.extractProduct()
        }
    }

    fun getProduct(name: String): Product {
        if (!productExists(name)) throw NoSuchProductIdException()

        return conn.prepareStatement("SELECT * FROM products WHERE name = ?").use {
            it.setString(1, name)
            val res = it.executeQuery()

            res.next()
            res.extractProduct()
        }
    }

    fun deleteProduct(id: Int) {
        if (!productExists(id)) throw NoSuchProductIdException()
//        if (productAmount(id) != 0) throw NonEmptyProductException() temporarily removed not to over complicate life fomymiself

        conn.createStatement().use {
            it.execute("DELETE FROM products WHERE id = $id")
        }
    }

    internal fun deleteProduct(name: String) {
        if (!productExists(name)) throw NoSuchProductIdException()

        conn.prepareStatement("DELETE FROM products WHERE name = ?").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun changeProductName(id: Int, name: String) {
        if (productExists(name)) throw NameTakenException()
        if (!productExists(id)) throw NoSuchProductIdException()
        if (!checkName(name)) throw WrongNameFormatException()

        conn.prepareStatement("UPDATE products SET name = ? WHERE id = $id").use {
            it.setString(1, name)

            it.executeUpdate()
        }
    }

    fun getGroup(id: Int): Group {
        if (!groupExists(id)) throw NoSuchGroupIdException()

        return conn.createStatement().use {
            val res = it.executeQuery("SELECT * FROM groups WHERE id = $id")
            res.next()
            res.extractGroup()
        }
    }

    fun changeGroupName(id: Int, name: String) {
        if (groupExists(name)) throw NameTakenException()
        if (!groupExists(id)) throw NoSuchGroupIdException()
        if (!checkName(name)) throw WrongNameFormatException()

        conn.prepareStatement("UPDATE groups SET name = ? WHERE id = $id").use {
            it.setString(1, name)

            it.executeUpdate()
        }
    }

    fun setPrice(id: Int, newPrice: Double) {
        if (!newPrice.isFinite() || newPrice <= 0) throw WrongPriceException()
        if (!productExists(id)) throw NoSuchProductIdException()

        conn.prepareStatement("UPDATE products SET price =  ? WHERE id = ?").use {
            it.setDouble(1, newPrice)
            it.setInt(2, id)

            it.executeUpdate()
        }
    }

    fun addItems(id: Int, number: Int) {
        if (number <= 0) throw IllegalArgumentException("Must be positive")
        if (!productExists(id)) throw NoSuchProductIdException()

        conn.createStatement().use {
            it.execute("UPDATE products SET quantity = quantity + $number WHERE id = $id ")
        }
    }

    fun addItems(name: String, number: Int) {
        if (number <= 0) throw IllegalArgumentException("Must be positive")
        if (!productExists(name)) throw NoSuchProductIdException()

        conn.prepareStatement("UPDATE products SET quantity = quantity + $number WHERE name = ?").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun removeItems(id: Int, number: Int) {
        if (number <= 0) throw IllegalArgumentException("Must be positive")
        if (!productExists(id)) throw NoSuchProductIdException()
        if (productAmount(id) < number) throw NotEnoughItemsException()

        conn.createStatement().use {
            it.execute("UPDATE products SET quantity = quantity - $number WHERE id = $id AND quantity >= $number")
        }
    }

    fun removeItems(name: String, number: Int) {
        if (number <= 0) throw IllegalArgumentException("Must be positive")
        if (!productExists(name)) throw NoSuchProductIdException()
        if (productAmount(name) < number) throw NotEnoughItemsException()

        conn.prepareStatement("UPDATE products SET quantity = quantity - $number WHERE name = ? AND quantity >= $number").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun productAmount(id: Int): Int {
        if (!productExists(id)) throw NoSuchProductIdException()

        return conn.createStatement().use {
            it.executeQuery("SELECT quantity FROM products WHERE id = $id").run {
                next()
                getInt("quantity")
            }
        }
    }

    fun productAmount(name: String): Int {
        if (!productExists(name)) throw NoSuchProductIdException()

        return conn.prepareStatement("SELECT quantity FROM products WHERE name = ?").use {
            it.setString(1, name)
            it.executeQuery().run {
                next()
                getInt("quantity")

            }
        }
    }

    fun totalSum(): Double {
        return conn.createStatement().use {
            it.executeQuery("SELECT SUM(price * quantity) as total from products").run {
                next()
                getDouble("total")
            }
        }
    }

    fun totalSumByGroup(id: Int): Double {
        return conn.createStatement().use {
            it.executeQuery("SELECT SUM(price * quantity) as total from products where groupId = $id").run {
                next()
                getDouble("total")
            }
        }
    }

    fun updateProduct(id: Int, name: String?, price: Double?, groupId: Int?) {
        if (!productExists(id)) throw NoSuchProductIdException()
        if (name != null) {
            if (productExists(name)) throw NameTakenException()
            if (!checkName(name)) throw WrongNameFormatException()
        }
        if (price != null) {
            if (!price.isFinite() || price <= 0) throw WrongPriceException()
        }
        if (groupId != null) {
            if (!groupExists(groupId)) throw NoSuchGroupIdException()
        }

        if (name != null) changeProductName(id, name)
        if (price != null) setPrice(id, price)
        if (groupId != null) setToGroup(id, groupId)

    }

    internal fun productExists(name: String): Boolean {
        return conn.prepareStatement("select count(*) as product_quantity from products where name = ?").use {
            it.setString(1, name)

            val res = it.executeQuery()
            res.next()
            res.getInt("product_quantity") != 0
        }
    }

    internal fun groupExists(name: String): Boolean {
        return conn.prepareStatement("select count(*) as group_quantity from groups where name = ?").use {
            it.setString(1, name)

            val res = it.executeQuery()
            res.next()
            res.getInt("group_quantity") != 0
        }
    }

    private fun productExists(id: Int): Boolean {
        return conn.createStatement().use {
            val res = it.executeQuery("select count(*) as product_quantity from products where id = $id")
            res.next()
            res.getInt("product_quantity") != 0
        }
    }

    internal fun groupExists(id: Int): Boolean {
        return conn.createStatement().use {
            val res = it.executeQuery("select count(*) as group_quantity from groups where id = $id")

            res.next()
            res.getInt("group_quantity") != 0
        }
    }

    internal fun deleteAll() {
        conn.createStatement().use {
            it.execute("DELETE FROM products")
            it.execute("DELETE FROM groups")
        }
    }

    override fun close() = conn.close()


    fun commit() = conn.commit()
    fun setAutoCommit(b: Boolean) {
        conn.autoCommit = b
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DaoProduct("file.db").use {
                val rice = Product("rice", 13.0)

                println(it.insertProduct(rice))

//        it.setToGroup(1, 1)
//        it.setToGroup(2, 1)
//        it.setToGroup(3, 1)

                it.getProductList(0, 200).forEach { product -> println(product) }
            }
        }
    }
}