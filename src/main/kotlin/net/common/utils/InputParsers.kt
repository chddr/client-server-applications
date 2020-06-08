package net.common.utils

import db.entities.Product
import org.json.JSONException
import org.json.JSONObject

object InputParsers {
    class ParseException(e: Throwable) : Exception(e)

    fun String.id(): Int {
        return try {
            JSONObject(this).getInt("id")
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }

    fun String.idAndName(): Pair<Int, String> {
        return try {
            JSONObject(this).run {
                getInt("id") to getString("name")
            }
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }

    fun String.idAndNumber(): Pair<Int, Int> {
        return try {
            JSONObject(this).run {
                getInt("id") to getInt("number")
            }
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }

    fun String.idAndPrice(): Pair<Int, Double> {
        return try {
            JSONObject(this).run {
                getInt("id") to getDouble("price")
            }
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }

    fun String.product(): Product {
        return try {
            JSONObject(this).run {
                Product(getString("name"), getDouble("price"))
            }
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }
}