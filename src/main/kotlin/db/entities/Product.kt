package db.entities

import org.json.JSONArray
import org.json.JSONObject

data class Product(val name: String, val price: Double, val id: Int? = null, val number: Int? = null, val groupId: Int? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (name != other.name) return false
        if (price != other.price) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + price.hashCode()
        return result
    }

    override fun toString(): String {
        return toJson().toString(2)
    }

    fun toJson() = JSONObject().apply {
        put("name", name)
        put("price", price)
        put("id", id)
        put("number", number)
        put("groupId", groupId)
    }

    companion object {
        fun Collection<Product>.toJsonString(): String = JSONArray().apply {
            for (product in this@toJsonString) put(product.toJson())
        }.toString(2)
    }
}