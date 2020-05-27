package warehouse

data class Item(val name: String, val price: Double, var group: Group? = null)