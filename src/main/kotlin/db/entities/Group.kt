package db.entities

data class Group(val id: Int, val name: String, val description: String? = null) {
    override fun toString() = name

}