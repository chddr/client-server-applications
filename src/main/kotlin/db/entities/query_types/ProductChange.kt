package db.entities.query_types

data class ProductChange(
        val id: Int,
        val name: String?,
        val price: Double?,
        val number: Int?,
        val groupId: Int?
)