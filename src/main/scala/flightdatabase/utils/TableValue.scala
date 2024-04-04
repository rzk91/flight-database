package flightdatabase.utils

import flightdatabase.domain.FlightDbTable.Table

case class TableValue[A](table: Table, value: A)
