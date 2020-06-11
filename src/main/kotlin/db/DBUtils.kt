package db

import db.entities.Criterion
import db.entities.Group
import db.entities.Product
import db.entities.User
import java.sql.ResultSet

object DBUtils {
    fun ResultSet.extractProduct(): Product {

        val groupId: Int? = getInt("groupId").takeIf { it != 0 }
        return Product(getString("name"), getDouble("price"), getInt("id"), getInt("quantity"), groupId)
    }

    fun ResultSet.extractGroup() = Group(getInt("id"), getString("name"))

    fun ResultSet.extractUser(): User {
        return User(
                id = getInt("id"),
                login = getString("login"),
                password = getString("password"),
                role = getString("role")
        )
    }

    fun checkName(name: String): Boolean {
        return name.matches(Regex("[\\w -]{3,20}")) && !name.startsWith(" ") && !name.endsWith(" ")
    }


    fun generateWhereClause(criterion: Criterion): String {
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



}