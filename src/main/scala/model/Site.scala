package model

import scala.collection.JavaConverters._
import java.net.URL
import java.util.Currency

import com.typesafe.scalalogging.LazyLogging
import model.Car._
import org.jsoup.Jsoup

sealed trait Site[M <: Model] {
    val name: String

    def url: URL
}

case class OtomotoSite(path: String = "") extends Site[Car] {

    override def url: URL = new URL(OtomotoSite.protocol + "://" + OtomotoSite.host + "/" + path)

    override val name: String = "OtoMoto"
}

object OtomotoSite extends LazyLogging {
    sealed trait CarType
    case object PassengerType extends CarType
    case object TransportType extends CarType

    sealed trait OtomotoFilter extends Filter[OtomotoSite]
    case class BrandFilter(brand: String)   extends OtomotoFilter
    case class TypeFilter(carType: CarType) extends OtomotoFilter
    case class UsageFilter(isUsed: Boolean) extends OtomotoFilter

    val protocol: String = "http"
    val host: String     = "www.otomoto.pl"

     def scrapeItem(itemUrl: URL): Option[Car] = {
        val link        = itemUrl.toString
        val response    = Jsoup.connect(link).ignoreContentType(true).execute()
        val contentType = response.contentType().split(";").head

        contentType match {
            case "text/html" => {
                val doc = response.parse()

                val carPriceElement = doc.getElementsByTag("span").asScala.filter(e => e.attr("class") == "offer-price__number")

                val carPriceValue        = carPriceElement.head.text().filter(_.isDigit).toInt
                val carPriceCurrencyCode = carPriceElement.head.child(0).text()

                val carParametersMap = doc.getElementsByTag("li").asScala
                    .filter(e => e.attr("class") == "offer-params__item")
                    .map(e => e.child(0).text() -> e.child(1).text())
                    .foldLeft(Map[String, String]())((map, pair) => map + pair)

                logger.trace("Item path: " + itemUrl.getPath)

                //Aliasing for the "get" method with logging code added
                def getParameter(key: String) = carParametersMap.get(key) match {
                    case Some(value) => Some(value)
                    case None        =>
                        logger.debug("Couldn't find key " + key + " for " + itemUrl.getPath)
                        None
                }

                for {
                    brand                <- getParameter("Marka")
                    model                <- getParameter("Model")
                    productionYear       <- getParameter("Rok produkcji").map(_.filter(_.isDigit).toInt)
                    mileage              <- getParameter("Przebieg").map(_.filter(_.isDigit).toInt)
                    engineVolume         <- getParameter("Pojemność skokowa").map(_.filter(_.isDigit).toInt)
                    horsePower           <- getParameter("Moc").map(_.filter(_.isDigit).toInt)
                    fuelType             <- getParameter("Rodzaj paliwa").flatMap(getFuelType)
                    transmissionType     <- getParameter("Skrzynia biegów").flatMap(getTransmissionType)
                    carClass             <- getParameter("Typ").flatMap(getCategory)
                    //doorCount            <- getParameter("Liczba drzwi").map(_.filter(_.isDigit).toInt)
                    //seatCount            <- getParameter("Liczba miejsc").map(_.filter(_.isDigit).toInt)
                    color                <- getParameter("Kolor")
                    countryOfOrigin      <- getParameter("Kraj pochodzenia")
                    isRegisteredInPoland <- getParameter("Zarejestrowany w Polsce").flatMap(getBoolean)
                    isFirstOwner         <- getParameter("Pierwszy właściciel").flatMap(getBoolean)
                    isAccidentFree       <- getParameter("Bezwypadkowy").flatMap(getBoolean)
                    isCertifiedServiced  <- getParameter("Serwisowany w ASO").flatMap(getBoolean)
                    isUsed               <- getParameter("Stan").flatMap(getUsageBoolean)
                } yield Car(
                    brand,
                    model,
                    productionYear,
                    mileage,
                    engineVolume,
                    horsePower,
                    fuelType,
                    transmissionType,
                    carClass,
                    //doorCount,
                    //seatCount,
                    color,
                    countryOfOrigin,
                    isRegisteredInPoland,
                    isFirstOwner,
                    isAccidentFree,
                    isCertifiedServiced,
                    isUsed,
                    carPriceValue,
                    Currency.getInstance(carPriceCurrencyCode))
            }
            case _ => None
        }
    }

    private def getFuelType(text: String) = text match {
        case "Benzyna"     => Some(Gasoline)
        case "Diesel"      => Some(Diesel)
        case "Benzyna+LPG" => Some(LPG)
        case _         =>
            logger.error("Failed to map string " + text + " with fuel type string to concrete type")
            None
    }

    private def getTransmissionType(text: String) = text match {
        case "Manualna"     => Some(Manual)
        case "Automatyczna" => Some(Automatic)
        case _              =>
            logger.error("Failed to map string " + text + " with transmission type string to concrete type")
            None
    }

    private def getCategory(text: String) = text match {
        case "Sedan/Limuzyna" => Some(Sedan)
        case "Hatchback"      => Some(Hatchback)
        case "Kombi"          => Some(Wagon)
        case "Kabriolet"      => Some(Convertible)
        case "Van (minibus)"  => Some(Van)
        case "SUV"            => Some(SUV)
        case _                =>
            logger.error("Failed to map string " + text + " with category to concrete type")
            None
    }

    private def getBoolean(text: String) = text match {
        case "Tak" => Some(true)
        case "Nie" => Some(false)
        case _     =>
            logger.error("Failed to map string " + text + " with boolean to concrete type")
            None
    }

    private def getUsageBoolean(text: String) = text match {
        case "Nowe"    => Some(false)
        case "Używane" => Some(true)
        case _         =>
            logger.error("Failed to map string " + text + " with usage state to concrete type")
            None
    }
}