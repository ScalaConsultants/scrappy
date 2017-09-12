import model.OtomotoSite
import scraper.Scraper

object Main extends App {
    import scraper.Scrapable._
    import model.Filterable._

    val typeFilter  = OtomotoSite.TypeFilter(OtomotoSite.PassengerType)
    val brandFilter = OtomotoSite.BrandFilter("Volkswagen")
    val usageFilter = OtomotoSite.UsageFilter(isUsed = true)

    //TODO: Currently the filter needs to be applied in correct order
    //Doing this on purely text might be hard, better is to construct a queue of filters where they each have priority/order info?
    val filteredSite = OtomotoSite().applyFilter(typeFilter).applyFilter(brandFilter).applyFilter(usageFilter)

    Scraper.scrapeToCSV(filteredSite)
}
