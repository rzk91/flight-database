package flightdatabase.persistence.syntax

trait AllPersistenceSyntax
    extends ToMoreConnectionIOOps
    with ToQueryOps
    with ToSqlStateOps
    with ToUpdateOps

object all extends AllPersistenceSyntax
