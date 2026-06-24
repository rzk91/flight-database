package flightdatabase.test.syntax

trait AllTestSyntax extends ToMatchersOps with ToIOResultOps with ToResponseIOOps

object all extends AllTestSyntax
