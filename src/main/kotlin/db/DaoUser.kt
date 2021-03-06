package db

import db.DBUtils.extractUser
import db.entities.User
import org.apache.commons.codec.digest.DigestUtils
import java.sql.Connection
import java.sql.DriverManager

class DaoUser(db: String) {

    private var connection: Connection = DriverManager.getConnection("jdbc:sqlite:$db")

    init {
        connection.createStatement().use { statement ->
            statement.execute("create table if not exists 'users'('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' VARCHAR(50) NOT NULL, 'password' VARCHAR(250) NOT NULL, 'role' VARCHAR(20) NOT NULL, UNIQUE (login))")
        }

    }

    fun insert(user: User): Int {
        return connection.prepareStatement("insert into 'users'('login', 'password', 'role') values (?, ?, ?)").use { statement ->
            statement.setString(1, user.login)
            statement.setString(2, DigestUtils.md5Hex(user.password))
            statement.setString(3, user.role)
            statement.execute()

            statement.generatedKeys.getInt("last_insert_rowid()")
        }
    }

    fun getUser(login: String): User? {
        return connection.prepareStatement("select * from 'users' where login = ?").use { statement ->
            statement.setString(1, login)

            val res = statement.executeQuery()
            when {
                res.next() -> res.extractUser()
                else -> null
            }
        }
    }

    fun delete(id: Int) {
        connection.createStatement().use { statement ->
            statement.execute("delete from users where id = $id")
        }
    }

    fun getUsers(): ArrayList<User> {
        return connection.createStatement().use {
            it.executeQuery("select * from 'users'").run {
                ArrayList<User>().also { prods ->
                    while (next())
                        prods.add(extractUser())
                }
            }
        }
    }
}