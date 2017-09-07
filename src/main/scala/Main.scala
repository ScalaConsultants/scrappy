import model.{Car, Filter, OtomotoSite}
import scraper.Scraper

object Main extends App {
    import scraper.Scrapable._
    import model.Filterable._

    val typeFilter  = OtomotoSite.TypeFilter(OtomotoSite.PassengerType)
    val brandFilter = OtomotoSite.BrandFilter("Fiat")
    val usageFilter = OtomotoSite.UsageFilter(isUsed = false)

    val otomotoSite = OtomotoSite()

    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")

    //TODO: Currently the filter needs to be applied in correct order
    //Doing this on purely text might be hard, better is to construct a queue of filters where they each have priority/order info?
    //Do it stateful -
    val filteredSite = otomotoSite.applyFilter(typeFilter).applyFilter(brandFilter)//.applyFilter(usageFilter)

    Scraper.scrapeToCSV(filteredSite)
}