package scraper

import java.io.File
import java.util.Calendar

import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.LazyLogging
import model.{Car, Site}
import scraper.Scrapable._
import model.Serializable._

import scala.util.{Failure, Success, Try}

object Scraper extends LazyLogging{
    //TODO: The site scraping shouldn't happen everytime we want to serialize to a different format
    //Maybe an algebraic type with possible targets + TC with methods for writing to it?

    def scrapeToCSV[S <: Site[_] : Scrapable](site: S) = {
        logger.debug("Attempting to scrape site: " + site.url.toString)

        val scrapingResult = site.scrape

        scrapingResult match {
            case Right(models) =>
                val directoryPath = site.url.getHost + site.url.getPath
                val filename      = Calendar.getInstance().getTime.toString + ".csv"

                val csvFile = for {
                    directory <- Try(new File(directoryPath))
                    _         <- Try(directory.mkdirs())
                    file      <- Try(new File(directoryPath + filename)) if directory.exists()
                    _         <- Try(file.createNewFile())
                } yield file

                csvFile match {
                    case Success(file) => {
                        //TODO: The pattern matching is needed cause the return type in scrape isn't bound by the type-class
                        //FIXME: Unsafe code (.head)
                        val headers = models.head match {
                            case car: Car => car.getHeaders
                        }

                        val serialized = models.map {
                            case car: Car => car.serialize
                        }

                        val writer = CSVWriter.open(file)

                        writer.writeAll(List(headers) ++ serialized)
                    }
                    case Failure(error) => logger.error("Failed IO operation: " + error.toString)
                }
            case Left(e) => logger.error("Error occurred during scraping: " + e)
        }
    }

    def scrapeToDB[T: Scrapable](site: T) = ???
}