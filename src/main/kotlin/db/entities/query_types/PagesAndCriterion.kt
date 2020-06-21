package db.entities.query_types

import db.entities.Criterion

data class PagesAndCriterion(val page: Int, val size: Int, val criterion: Criterion?)