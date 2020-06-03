package pr4

import pr4.entities.Criteria
import pr4.entities.Product
import java.sql.Connection
import java.sql.DriverManager

class DaoProduct(val db: String) {

    private val connection: Connection

    init {

        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$db")

        initTable()
    }

    private fun initTable() {
        connection.createStatement().use {
            it.execute(
                    "create table if not exists 'products' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text not null, 'price' REAL not null, unique (name) )"
            )
        }
    }

    fun insertProduct(product: Product): Int {
        connection.prepareStatement("insert into products('name', 'price') values (?,?)").use {
            connection.autoCommit = false

            it.setString(1, product.name)
            it.setDouble(2, product.price)

            it.executeUpdate()

            val res = it.generatedKeys

            connection.commit()

            return res.getInt("last_insert_rowid()")
        }
    }

    fun getProductList(page: Int, size: Int, criteria: Criteria): ArrayList<Product> {
        connection.createStatement().use {
            val res = it.executeQuery(
                    "select * from products LIMIT $size OFFSET ${page * size}"
            )

            val products = ArrayList<Product>()

            while (res.next()) {
                products.add(
                        Product(
                                res.getInt("id"),
                                res.getString("name"),
                                res.getDouble("price")
                        ))
            }

            return products
        }
    }

    fun checkIfTaken(prodName: String): Boolean {
        connection.createStatement().use {
            var res = it.executeQuery(
                    "select count(*) as number_of_products from products where name = '$prodName'"
            )

            res.next()

            return res.getInt("number_of_products") == 0
        }
    }

    fun deleteAll() {

    }

}


fun main() {
    val dao = DaoProduct("file.db")

    val name = "grecha"

    for (i in 0..19) {
        val product = Product("prod $i", i.toDouble())
        val id = dao.insertProduct(product)
    }

    dao.getProductList(0, 200, Criteria()).forEach{println(it)}

    val criteria = Criteria()

    println(dao.getProductList(0, 100))
}