trait Scrapable[T] {
    def scrape(site: T): Seq[Car]
}