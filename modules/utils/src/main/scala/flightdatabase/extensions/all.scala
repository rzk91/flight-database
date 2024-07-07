package flightdatabase.extensions

trait AllExtensions
    extends ToMoreConnectionIOOps
    with ToDoubleOps
    with ToIterableOps
    with ToKleisliResponseOps
    with ToOptionOps
    with ToPathOps
    with ToQueryOps
    with ToSqlStateOps
    with ToStringOps
    with ToTryOps
    with ToUpdateOps

trait AllTestExtensions extends ToResponseIOOps with ToMatchersOps with ToIOResultOps

object all extends AllExtensions

object test extends AllTestExtensions
