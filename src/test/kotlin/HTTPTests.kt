
import DaoProductTest.Companion.populate
import db.entities.Product
import db.entities.User
import db.entities.UserCredentials
import db.entities.query_types.Id
import db.entities.query_types.IdAndName
import http.HttpServer
import http.responses.LoginResponse
import io.restassured.RestAssured
import io.restassured.RestAssured.`when`
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HTTPTests {

    private var server = HttpServer()
    private val port = 9568

    private val adminLogin = "test"
    private val adminPass = "test"

    @BeforeAll
    fun init() {
        server = HttpServer(port, "testing.db:memory")
        server.productDB.populate()
        //set up an admin role
        setUpAdmin()
        RestAssured.port = port
    }

    @AfterAll
    fun tearDown() {
        server.productDB.deleteAll()
        server.close()
    }

    private fun setUpAdmin() {

        if (server.userDB.getUser(adminLogin) == null)
            server.userDB.insert(User(
                    adminLogin,
                    adminPass,
                    "admin"
            ))
    }

    @Test
    fun testLogin() {
        given().body(UserCredentials(adminLogin, adminPass))
                .`when`().get("/login")
                .then().statusCode(200).body("token", not(emptyOrNullString()))
    }


    @Test
    fun productId_withNoToken() {
        `when`().get("/api/product/4")
                .then().statusCode(403).body("message", `is`("No permission"))
    }

    @Test
    fun productLifecycle() {
        val (loginToken, _, _) = getToken()

        val name = "test_name"
        val price = 100.0
        //insert a product
        val id = insertProduct(loginToken, name, price)
        //get product
        var prod = getProduct(loginToken, id)

        Assertions.assertTrue(name == prod.name)
        Assertions.assertTrue(price == prod.price)
        Assertions.assertTrue(id == prod.id)

        //modify product
        val newName = "NEW_NAME"
        given().header("Authorization", loginToken)
                .body(IdAndName(id, newName))
                .`when`().post("/api/product")
                .then().statusCode(204).body(emptyOrNullString())

        //get product again
        prod = getProduct(loginToken, id)
        Assertions.assertTrue(newName == prod.name)

        //delete product
        given().header("Authorization", loginToken)
                .`when`().delete("/api/product/{id}", id)
                .then().statusCode(204).body(emptyOrNullString())
    }

    private fun insertProduct(loginToken: String, name: String, price: Double): Int {
        return given().header("Authorization", loginToken).body(Product(name, price))
                .`when`().put("/api/product")
                .then().statusCode(201).body("id", notNullValue())
                .extract().`as`(Id::class.java).id
    }

    private fun getProduct(loginToken: String, id: Int): Product {
        return given().header("Authorization", loginToken)
                .`when`().get("/api/product/{id}", id)
                .then().statusCode(200)
                .extract().`as`(Product::class.java)
    }

    @Test
    fun loginHandlerExceptions() {
        given().`when`().patch("/login")
                .then().statusCode(405).body("message", containsString("not allowed"))

        given().body("MaLfOrMeD mEsSAge").`when`().get("/login")
                .then().statusCode(400).body("message", `is`("Bad request"))

        given().body(UserCredentials(adminLogin, adminPass + "WRONG")).`when`().get("/login")
                .then().statusCode(401).body("message", `is`("Invalid password"))

        given().body(UserCredentials(adminLogin + "WRONG", adminPass)).`when`().get("/login")
                .then().statusCode(401).body("message", `is`("Invalid user"))
    }

    @Test
    fun productHandlerExceptions() {
        `when`().patch("/api/product")
                .then().statusCode(403).body("message", `is`("No permission"))

        val (loginToken, _, _) = getToken()

        given().header("Authorization", loginToken)
                .`when`().patch("/api/product")
                .then().statusCode(405).body("message", containsString("method not allowed"))


    }

    @Test
    fun productIdHandlerExceptions() {
        `when`().patch("/api/product/1")
                .then().statusCode(403).body("message", `is`("No permission"))

        val (loginToken, _, _) = getToken()

        given().header("Authorization", loginToken)
                .`when`().get("/api/product/4012984091284091822109401294")
                .then().statusCode(400).body("message", containsString("Id too long"))

        given().header("Authorization", loginToken)
                .`when`().patch("/api/product/1")
                .then().statusCode(405).body("message", containsString("method not allowed"))


    }


    private fun getToken(login: String = adminLogin, password: String = adminPass): LoginResponse {
        return given().body(UserCredentials(login, password))
                .`when`().get("/login")
                .then().statusCode(200).body("token", not(emptyOrNullString()))
                .extract().`as`(LoginResponse::class.java)
    }
}
