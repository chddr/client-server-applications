package pr4

import pr4.entities.Product
import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DaoProduct(db: String): Closeable {

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
    fun insert(product: Product): Int? {
        return conn.prepareStatement("insert into products('name', 'price') values (?,?)").use {
            it.runCatching {
                setString(1, product.name)
                setDouble(2, product.price)

                executeUpdate()
                generatedKeys
            }.getOrNull()?.getInt("last_insert_rowid()")
        }
    }

    fun getList(page: Int = 0, size: Int = 20): ArrayList<Product> {
        if (page < 0 || size <= 0) throw IllegalArgumentException("wrong parameters")

        return conn.createStatement().use {
            it.executeQuery(
                    "select * from products limit $size offset ${page * size}"
            ).run {
                ArrayList<Product>().also { prods ->
                    while (next())
                        prods.add(extractProduct())
                }
            }
        }
    }

    fun get(id: Int): Product? {
        return conn.createStatement().use {
            val res = it.executeQuery("select * from products where id = $id")

            when {
                res.next() -> res.extractProduct()
                else -> null
            }
        }
    }

    fun get(name: String): Product? {
        return conn.prepareStatement("select * from products where name = ?").use {
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
            it.execute("delete from products where id = $id")
        }
    }

    fun delete(name: String) {
        conn.prepareStatement("delete from products where name = ?").use {
            it.setString(1, name)
            it.execute()
        }
    }

    fun setPrice(id: Int, newPrice: Double) {
        conn.prepareStatement("update products set price = ? WHERE id = ?").use {
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

    fun populate() {
        listOf(
                Product("buckwheat", 9.99),
                Product("rice", 13.0),
                Product("wheat", 2.0),
                Product("tofu", 25.0),
                Product("quinoa", 999.0),
                Product("banana", 3.0),
                Product("kale", 9.0),
                Product("cabbage", 9.0)

        ).forEach { insert(it) }
    }

    override fun close() = conn.close()

}


fun main() {
    DaoProduct("file.db").use {
        val buckwheat = Product("buckwheat", 9.99)
        val rice = Product("rice", 13.0)

        assert(it.get("buckwheat") != null)
        assert(it.get("buckwheat1") == null)

        println(it.insert(buckwheat))
        println(it.insert(rice))

        it.setPrice(1, 14.4)


        it.getList(0, 200).forEach { println(it) }
    }

}