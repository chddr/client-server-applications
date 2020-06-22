package db.entities

data class Group(val id: Int, val name: String) {
    override fun toString() = name

}