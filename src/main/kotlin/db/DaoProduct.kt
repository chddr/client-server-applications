package db

import db.entities.Criterion
import db.entities.Product
import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DaoProduct(db: String) : Closeable {

    open class DBException : Exception()
    class NoSuchProductIdException : DBException()
    class NoSuchGroupIdException : DBException()
    class NotEnoughItemsException : DBException()
    class NameTakenException : DBException()

    private val conn: Connection = DriverManager.getConnection("jdbc:sqlite:$db")

    init {
        conn.createStatement().use {
            it.execute("CREATE TABLE IF NOT EXISTS 'groups' ('id' INTEGER PRIMARY KEY AUTOINCREMENT , 'name' TEXT NOT NULL UNIQUE)")
            it.execute("CREATE TABLE IF NOT EXISTS 'products' ('id' INTEGER PRIMARY KEY AUTOINCREMENT , 'name' TEXT NOT NULL UNIQUE , 'price' REAL NOT NULL, 'quantity' INTEGER NOT NULL DEFAULT 0, 'groupId' INTEGER, FOREIGN KEY (groupId) REFERENCES groups(id))")
        }
    }

    /**
     * Inserts a product and returns it's id in the table if everything went ok, otherwise null
     */
    fun insert(product: Product): Int {
        if (productExists(product.name)) throw NameTakenException()

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

        return conn.prepareStatement("INSERT INTO groups('name') VALUES (?)").use {
            it.run {
                setString(1, name)

                executeUpdate()
                generatedKeys
            }.getInt("last_insert_rowid()")
        }
    }

    fun setToGroup(prodId: Int, groupId: Int) {
        if (!groupExists(groupId)) throw NoSuchGroupIdException()
        if (!productExists(prodId)) throw NoSuchProductIdException()

        conn.createStatement().use {
            it.execute("UPDATE products SET groupId = $groupId WHERE id = $prodId")
        }
    }

    fun getList(page: Int = 0, size: Int = 20, criterion: Criterion = Criterion()): ArrayList<Product> {
        if (page < 0 || size <= 0) throw IllegalArgumentException("wrong parameters")

        return conn.createStatement().use {
            val conditions = generateWhereClause(criterion)
            val query = "SELECT * FROM products $conditions LIMIT $size OFFSET ${page * size}"

//            println(query)
            it.executeQuery(query).run {
                ArrayList<Product>().also { prods ->
                    while (next())
                        prods.add(extractProduct())
                }
            }
        }
    }

    private fun generateWhereClause(criterion: Criterion): String {
        val conditions = listOfNotNull(
                like(criterion.query),
                inIds(criterion.ids),
                range(criterion.lower, criterion.upper),
                group(criterion.groupId)

        ).ifEmpty { return "" }.joinToString(" AND ")

        return "WHERE $conditions"
    }

    private fun group(groupId: Int?, field: String = "groupId"): String? {
        if (groupId == null) return null
        return "$field = $groupId"
    }

    //TODO not sql-injection safe
    private fun like(query: String?, field: String = "name"): String? {
        if (query == null) return null
        return "$field LIKE '%$query%'"
    }

    private fun inIds(ids: Set<Int>?, field: String = "id"): String? {
        if (ids == null || ids.isEmpty()) return null
        return "$field IN (${ids.joinToString()})"
    }

    private fun range(lower: Double?, upper: Double?, field: String = "price"): String? = when {
        lower != null && upper != null -> "$field BETWEEN $lower AND $upper"
        lower != null -> "$field >= $lower"
        upper != null -> "$field <= $upper"
        else -> null
    }

    fun get(id: Int): Product? {
        return conn.createStatement().use {
            val res = it.executeQuery("SELECT * FROM products WHERE id = $id")

            when {
                res.next() -> res.extractProduct()
                else -> null
            }
        }
    }

    fun get(name: String): Product? {
        return conn.prepareStatement("SELECT * FROM products WHERE name = ?").use {
            it.setString(1, name)
            val res = it.executeQuery()

            when {
                res.next() -> res.extractProduct()
                else -> null
            }
        }
    }

    fun delete(id: Int) {
        if (!productExists(id)) throw NoSuchProductIdException()

        conn.createStatement().use {
            it.execute("DELETE FROM products WHERE id = $id")
        }
    }

    fun delete(name: String) {
        if (!productExists(name)) throw NoSuchProductIdException()

        conn.prepareStatement("DELETE FROM products WHERE name = ?").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun setPrice(id: Int, newPrice: Double) {
        if (!newPrice.isFinite() || newPrice <= 0) throw IllegalArgumentException("Price is wrong")
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
        if (amount(id)!! < number) throw NotEnoughItemsException()

        conn.createStatement().use {
            it.execute("UPDATE products SET quantity = quantity - $number WHERE id = $id AND quantity >= $number")
        }
    }

    fun removeItems(name: String, number: Int) {
        if (number <= 0) throw IllegalArgumentException("Must be positive")
        if (!productExists(name)) throw NoSuchProductIdException()
        if (amount(name)!! < number) throw NotEnoughItemsException()

        conn.prepareStatement("UPDATE products SET quantity = quantity - $number WHERE name = ? AND quantity >= $number").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun amount(id: Int): Int? {
        return conn.createStatement().use {
            it.executeQuery("SELECT quantity FROM products WHERE id = $id").run {
                when {
                    next() -> getInt("quantity")
                    else -> null
                }
            }
        }
    }

    fun amount(name: String): Int? {
        return conn.prepareStatement("SELECT quantity FROM products WHERE name = ?").use {
            it.setString(1, name)
            it.executeQuery().run {
                when {
                    next() -> getInt("quantity")
                    else -> null
                }
            }
        }
    }

    fun productExists(name: String): Boolean {
        return conn.prepareStatement("select count(*) as product_quantity from products where name = ?").use {
            it.setString(1, name)

            val res = it.executeQuery()
            res.next()
            res.getInt("product_quantity") != 0
        }
    }

    fun groupExists(name: String): Boolean {
        return conn.prepareStatement("select count(*) as group_quantity from groups where name = ?").use {
            it.setString(1, name)

            val res = it.executeQuery()
            res.next()
            res.getInt("group_quantity") != 0
        }
    }

    fun productExists(id: Int): Boolean {
        return conn.createStatement().use {
            val res = it.executeQuery("select count(*) as product_quantity from products where id = $id")
            res.next()
            res.getInt("product_quantity") != 0
        }
    }

    fun groupExists(id: Int): Boolean {
        return conn.createStatement().use {
            val res = it.executeQuery("select count(*) as group_quantity from groups where id = $id")

            res.next()
            res.getInt("group_quantity") != 0
        }
    }

    private fun ResultSet.extractProduct(): Product {

        val groupId: Int? = getInt("groupId").takeIf { it != 0 }
        return Product(getString("name"), getDouble("price"), getInt("id"), getInt("quantity"), groupId)
    }

    fun deleteAll() {
        conn.createStatement().use {
            it.execute("DELETE FROM products")
            it.execute("DELETE FROM groups")
        }
    }

    override fun close() = conn.close()
}

fun main() {
    DaoProduct("file.db").use {
//        val rice = Product("rice", 13.0)
//
//        println(it.insert(rice))

        it.setToGroup(1, 1)
        it.setToGroup(2, 1)
        it.setToGroup(3, 1)

        it.getList(0, 200).forEach { println(it) }
    }

}