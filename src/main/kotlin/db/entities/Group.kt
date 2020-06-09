package db.entities

import org.json.JSONArray
import org.json.JSONObject

data class Group(val id: Int, val name: String) {
    override fun toString(): String = toJson().toString(2)

    fun toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
    }

    companion object {
        fun Collection<Group>.toJsonString(): String = JSONArray().apply {
            for (group in this@toJsonString) put(group.toJson())
        }.toString(2)
    }
}