package pr4.entities

data class Product(val id: Int?, val name: String, val price: Double) {
    constructor(id: String, name: Double) : this(null, id, name)

}