import scala.collection.JavaConverters._

import java.net.URL
import org.jsoup.Jsoup

trait Indexable[T] {
    def getIndexes(site: T): Either[IndexingError, Seq[URL]]
}

sealed trait IndexingError
case object UnknownContentType extends IndexingError

object Indexable {
    def apply[T](implicit indexable: Indexable[T]): Indexable[T] = indexable

    def getIndexes[T: Indexable](t: T) = Indexable[T].getIndexes(t)

    implicit class IndexableOps[T: Indexable](indexable: T) {
        def getIndexes = Indexable[T].getIndexes(indexable)
    }

    implicit val OtomotoSiteIsIndexable: Indexable[OtomotoSite.type] =
        site => {
            val link: String = site.url.toString

            val response = Jsoup.connect(link).ignoreContentType(true).execute()

            val contentType: String = response.contentType().split(";").head

            contentType match {
                case "text/html" => {
                    val doc = response.parse()

                    val indexSelectorContent = doc.getElementsByTag("span").asScala.filter(e => e.attr("class") == "page").map(e => e.text())
                    val lastPageNumber = indexSelectorContent.filter(t => t.forall(_.isDigit)).map(_.toInt).max

                    Right(Range(1, lastPageNumber).map { pageNumber =>
                        new URL(link + s"&page=$pageNumber")
                    })
                }
                case _ => Left(UnknownContentType)
            }
        }
}