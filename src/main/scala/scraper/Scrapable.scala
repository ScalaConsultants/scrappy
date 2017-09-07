package scraper

import scala.collection.JavaConverters._
import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import model.{Model, OtomotoSite, Site}
import org.jsoup.Jsoup

trait Scrapable[S] {
    def scrape(site: S): Either[ScrappingError, Seq[Model]]
}

sealed trait ScrappingError
case object UnknownContentType extends ScrappingError

object Scrapable extends LazyLogging {
    def apply[S](implicit scrapable: Scrapable[S]): Scrapable[S] = scrapable

    def scrape[S: Scrapable](t: S)  = Scrapable[S].scrape(t)

    implicit class ScrapableOps[S: Scrapable](scrapable: S) {
        def scrape  = Scrapable[S].scrape(scrapable)
    }

    implicit val OtomotoSiteIsScrapable: Scrapable[OtomotoSite] =
        new Scrapable[OtomotoSite] {

            def getName(site: OtomotoSite): String = site.name

            def scrape(site: OtomotoSite): Either[ScrappingError, Seq[Model]] = {
                val link        = site.url.toString
                val response    = Jsoup.connect(link).ignoreContentType(true).execute()
                val contentType = response.contentType().split(";").head

                contentType match {
                    case "text/html" => {
                        val doc = response.parse()

                        val indexSelectorContent = doc.getElementsByTag("span").asScala.filter(e => e.attr("class") == "page").map(e => e.text())
                        val lastPageNumber = indexSelectorContent.filter(t => t.forall(_.isDigit)).map(_.toInt).max

                        Right(Range(1, lastPageNumber).flatMap { pageNumber =>
                            val indexLink        = link + s"&page=$pageNumber"
                            val indexResponse    = Jsoup.connect(indexLink).ignoreContentType(true).execute()
                            val indexContentType = response.contentType().split(";").head

                            logger.debug("Scraping page: " + pageNumber)

                            indexContentType match {
                                case "text/html" => {
                                    val indexDoc = indexResponse.parse()

                                    indexDoc.getElementsByTag("a").asScala
                                        .filter(e => e.attr("class") == "offer-title__link")
                                        .map(e => e.attr("href"))
                                        .map(itemLink => new URL(itemLink))
                                        .flatMap(itemUrl => OtomotoSite.scrapeItem(itemUrl))
                                        .toList
                                }
                                case _ => Nil
                            }
                        })
                    }
                    case _ => Left(UnknownContentType)
                }
            }
        }
}