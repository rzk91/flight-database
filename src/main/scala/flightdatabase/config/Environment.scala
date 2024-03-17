package flightdatabase.config

sealed trait Environment
case object DEV extends Environment
case object PROD extends Environment
