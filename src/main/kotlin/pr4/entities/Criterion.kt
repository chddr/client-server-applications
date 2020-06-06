package pr4.entities

class Criterion(var query: String? = null, var ids: Set<Int>? = null, var lower: Double? = null, var upper: Double? = null) {

    fun query(query: String?): Criterion {
        this.query = query
        return this
    }

    fun ids(ids: Set<Int>?): Criterion {
        this.ids = ids
        return this
    }

    fun lower(lower: Double?): Criterion {
        this.lower = lower
        return this
    }

    fun upper(upper: Double?): Criterion {
        this.upper = upper
        return this
    }


}