package net.common.utils

import com.fasterxml.jackson.module.kotlin.readValue
import db.entities.Product
import net.common.Processor.OBJECT_MAPPER
import net.common.utils.query_types.*

object InputParsers {
    class ParseException(e: Throwable) : Exception(e)

    fun String.name(): String {
        return try {
            OBJECT_MAPPER.readValue<Name>(this).name
        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

    fun String.id(): Int {
        return try {
            OBJECT_MAPPER.readValue<Id>(this).id
        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

    fun String.idAndName(): IdAndName {
        return try {
            OBJECT_MAPPER.readValue(this)
        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

    fun String.idAndNumber():IdAndNumber {
        return try {
            OBJECT_MAPPER.readValue(this)
        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

    fun String.idAndPrice(): IdAndPrice {
        return try {
            OBJECT_MAPPER.readValue(this)
        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

    fun String.product(): Product {
        return try {
            OBJECT_MAPPER.readValue(this)
        } catch (e: Exception) {            e.printStackTrace()

            throw ParseException(e)
        }
    }
}