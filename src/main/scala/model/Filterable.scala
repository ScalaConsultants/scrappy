package model

import java.net.URL

trait Filterable[S <: Site[_]] {
    def applyFilter(site: S, filter: Filter[S]): S
}

trait Filter[S <: Site[_]]

object Filterable {
    def apply[S <: Site[_]](implicit filterable: Filterable[S]): Filterable[S] = filterable

    def applyFilter[S <: Site[_] : Filterable](site: S, filter: Filter[S]) = Filterable[S].applyFilter(site, filter)

    implicit class FilterableOps[S <: Site[_] : Filterable](filterable: S) {
        def applyFilter(filter: Filter[S]) = Filterable[S].applyFilter(filterable, filter)
    }

    //TODO: This should be remade better to depend less on string operations
    implicit val OtomotoSiteIsFilterable: Filterable[OtomotoSite] =
        new Filterable[OtomotoSite] {
            import model.OtomotoSite._

            def applyFilter(site: OtomotoSite, filter: Filter[OtomotoSite]): OtomotoSite = filter match {
                case TypeFilter(carType) => {
                    val appendix = carType match {
                        case PassengerType => "/osobowe/"
                        case TransportType => "/dostawcze/"
                        case _             => ""
                    }

                    OtomotoSite(site.path + appendix)
                }
                case BrandFilter(brand) => {
                    val appendix = "/" + brand.toLowerCase + "/"

                    OtomotoSite(site.path + appendix)
                }
                case UsageFilter(isUsed: Boolean) => {
                    val usedValue = if (isUsed) "used" else "new"
                    val appendix = "/?search[new_used]=/" + usedValue

                    OtomotoSite(site.path + appendix)
                }
            }
        }
}