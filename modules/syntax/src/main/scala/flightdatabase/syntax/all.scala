package flightdatabase.syntax

trait AllExtensions
    extends ToDoubleOps
    with ToIterableOps
    with ToOptionOps
    with ToPathOps
    with ToStringOps
    with ToTryOps

object all extends AllExtensions
