package flightdatabase.api.testutils.endpoints

import flightdatabase.FieldType
import flightdatabase.Operator
import io.circe.Decoder

/**
  * A single filterable field of a known DB type, paired with a sample value and an operator that is
  * valid for that type. Used to drive the generic `/filter` and `return-only` scenarios: an entity
  * declares one fixture per field type it actually has, and coverage scales with the data rather
  * than with copy-pasted, type-specialised scenarios.
  *
  * The `Put`/`Read`/`Decoder` evidence rides along on each fixture so the (type-erased) list of
  * fixtures can be unpacked and replayed without the call site needing to summon instances for an
  * existential `V`.
  */
final case class FieldFixture[V](
  field: String,
  value: V,
  operator: Operator,
  fieldType: FieldType[V]
)(implicit val decoder: Decoder[V]) {
  def valueString: String = value.toString
}
