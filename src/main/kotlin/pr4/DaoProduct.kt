package pr4

import pr4.entities.Criterion
import pr4.entities.Product
import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DaoProduct(db: String) : Closeable {

    private val conn: Connection = DriverManager.getConnection("jdbc:sqlite:$db")

    init {
        conn.createStatement().use {
            it.execute(
                    "create table if not exists 'products' ('id' integer primary key autoincrement , 'name' text not null unique , 'price' real not null)"
            )
        }
    }

    /**
     * Inserts a product and returns it's id in the table if everything went ok, otherwise null
     */
    fun insert(product: Product): Int {
        return conn.prepareStatement("INSERT INTO products('name', 'price') VALUES (?,?)").use {
            it.run {
                setString(1, product.name)
                setDouble(2, product.price)

                executeUpdate()
                generatedKeys
            }.getInt("last_insert_rowid()")
        }
    }

    fun getList(page: Int = 0, size: Int = 20, criterion: Criterion = Criterion()): ArrayList<Product> {
        if (page < 0 || size <= 0) throw IllegalArgumentException("wrong parameters")

        return conn.createStatement().use {
            val conditions = generateWhereClause(criterion)
            val query = "SELECT * FROM products $conditions LIMIT $size OFFSET ${page * size}"

            println(query)
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
                range(criterion.lower, criterion.upper)

        ).ifEmpty { return "" }.joinToString(" AND ")

        return "WHERE $conditions"

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
        conn.createStatement().use {
            it.execute("DELETE FROM products WHERE id = $id")
        }
    }

    fun delete(name: String) {
        conn.prepareStatement("DELETE FROM products WHERE name = ?").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun setPrice(id: Int, newPrice: Double) {
        conn.prepareStatement("UPDATE products SET price = ? WHERE id = ?").use {
            it.setDouble(1, newPrice)
            it.setInt(2, id)
            it.execute()
        }
    }

    fun isTaken(name: String) = get(name) != null

    private fun ResultSet.extractProduct(): Product =
            Product(getString("name"), getDouble("price"), getInt("id"))

    fun deleteAll() {
        conn.createStatement().use {
            it.execute("delete from products")
        }
    }

    override fun close() = conn.close()

}

fun main() {
    DaoProduct("file.db").use {
//        val buckwheat = Product("buckwheat", 9.99)
//        val rice = Product("rice", 13.0)

        assert(it.isTaken("buckwheat"))
        assert(!it.isTaken("buckwheat1"))

        //println(it.insert(buckwheat))
        //println(it.insert(rice))

        it.getList(0, 200).forEach { println(it) }
    }

}