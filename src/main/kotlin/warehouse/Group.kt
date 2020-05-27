package warehouse

data class Group(var name: String, val items: Set<Item> = HashSet())