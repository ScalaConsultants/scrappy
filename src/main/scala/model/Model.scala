package model

import java.util.{Currency, Date}

import model.Car._

sealed trait Model
case class Car (brand: String,
                model: String,
                productionYear: Int,
                mileage: Int,
                //engineVolumeCubicCentimeters: Int,
                horsePower: Int,
                fuelType: FuelType,
                transmissionType: TransmissionType,
                carClass: Category,
                //doorCount: Int,
                //seatCount: Int,
                color: String,
                countryOfOrigin: String,
                isRegisteredInPoland: Boolean,
                isFirstOwner: Boolean,
                isAccidentFree: Boolean,
                isCertifiedAutoRepairServiced: Boolean,
                isUsed: Boolean,
                price: Int,
                currency: Currency) extends Model

object Car {
    sealed trait Parameter {
        val serialized: String
    }

    sealed trait FuelType extends Parameter
    case object Gasoline extends FuelType {val serialized = "Gasoline"}
    case object Diesel   extends FuelType {val serialized = "Diesel"}
    case object LPG      extends FuelType {val serialized = "LPG"}
    case object Hybrid   extends FuelType {val serialized = "Hybrid"}

    sealed trait TransmissionType extends Parameter
    case object Manual    extends TransmissionType {val serialized = "Manual"}
    case object Automatic extends TransmissionType {val serialized = "Automatic"}

    sealed trait Category   extends Parameter
    case object Sedan       extends Category {val serialized = "Sedan"}
    case object Hatchback   extends Category {val serialized = "Hatchback"}
    case object Wagon       extends Category {val serialized = "Wagon"}
    case object Convertible extends Category {val serialized = "Convertible"}
    case object Van         extends Category {val serialized = "Van"}
    case object Minivan     extends Category {val serialized = "Minivan"}
    case object SUV         extends Category {val serialized = "SUV"}
    case object Pickup      extends Category {val serialized = "Pickup"}
}
