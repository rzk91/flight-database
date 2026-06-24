package flightdatabase.persistence.syntax

trait AllPersistenceSyntax
    extends ToMoreConnectionIOOps
    with ToFieldTypeOps
    with ToQueryOps
    with ToSortAndLimitOps
    with ToSqlStateOps
    with ToUpdateOps

object all extends AllPersistenceSyntax
