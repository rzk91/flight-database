package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.testutils.RepositoryCheck

final class ManufacturerRepositoryIT extends RepositoryCheck {

  "Selecting all manufacturers" should "return the correct detailed list" in {
    val manufacturers = {
      for {
        repo             <- ManufacturerRepository.make[IO]
        allManufacturers <- repo.getManufacturers
      } yield allManufacturers
    }.unsafeRunSync().value.value

    manufacturers should not be empty
    manufacturers should contain only (
      Manufacturer(1, "Airbus", 5),
      Manufacturer(2, "Boeing", 6)
    )
  }
}
