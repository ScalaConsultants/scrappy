import java.net.URL

sealed trait Site{
    val url: URL
}

case object OtomotoSite extends Site {
    override val url: URL = new URL ("http://www.otomoto.pl/osobowe/volkswagen/?search[new_used]=used")
}