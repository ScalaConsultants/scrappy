package model

trait Serializable[M] {
    def getHeaders(model: M): Seq[String]
    def serialize(model: M): Seq[String]
}

object Serializable {
    def apply[M](implicit serializable: Serializable[M]): Serializable[M] = serializable

    def getHeaders[M: Serializable](t: M) = Serializable[M].getHeaders(t)
    def serialize[M: Serializable](t: M)  = Serializable[M].serialize(t)

    implicit class SerializableOps[M: Serializable](serializable: M) {
        def getHeaders = Serializable[M].getHeaders(serializable)
        def serialize  = Serializable[M].serialize(serializable)
    }

    implicit val CarIsSerializable: Serializable[Car] =
        new Serializable[Car] {
            def getHeaders(model: Car): Seq[String] = {
                Seq("Brand",
                    "Model",
                    "Production Year",
                    "Mileage",
                    "Engine Volume (cm3)",
                    "Horse Power",
                    "Fuel Type",
                    "Transmission Type",
                    "Class",
                    //"Door Count",
                    //"Seat Count",
                    "Color",
                    "Country Of Origin",
                    "Registered In Poland",
                    "First Owner",
                    "Without Accidents",
                    "Certified Auto Repair Serviced",
                    "Usage State",
                    "Price",
                    "Currency")
            }

            def serialize(model: Car): Seq[String] = {
                Seq(model.brand,
                    model.model,
                    model.productionYear.toString,
                    model.mileage.toString,
                    model.engineVolumeCubicCentimeters.toString,
                    model.horsePower.toString,
                    model.fuelType.serialized,
                    model.transmissionType.serialized,
                    model.carClass.serialized,
                    //model.doorCount.toString,
                    //model.seatCount.toString,
                    model.color,
                    model.countryOfOrigin,
                    model.isRegisteredInPoland.toString,
                    model.isFirstOwner.toString,
                    model.isAccidentFree.toString,
                    model.isCertifiedAutoRepairServiced.toString,
                    model.isUsed.toString,
                    model.price.toString,
                    model.currency.getCurrencyCode)
            }
        }
}