package com.example.neoradio.api

import com.example.neoradio.model.HomePage
import com.example.neoradio.model.Station
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parseInputStream
import okhttp3.OkHttpClient
import okhttp3.Request

object ERadio {
    private const val baseUrl = "https://www.e-radio.gr"
    private val client = OkHttpClient.Builder()
        .build()

    private fun get(endpoint: String) =
        client.newCall(
            Request.Builder().url(endpoint.let {
                if (endpoint.startsWith("http")) {
                    endpoint
                } else {
                    baseUrl + endpoint
                }
            })
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:153.0) Gecko/20100101 Firefox/153.0"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .header("Priority", "u=0, i")
                .build()
        ).execute()

    private fun <T> getHTML(endpoint: String, onParse: (document: Document) -> T): T {
        val response = get(endpoint)

        return onParse(Ksoup.parseInputStream(response.body.byteStream(), baseUri = baseUrl)).also {
            response.close()
        }
    }

    private fun parseStation(li: Element): Station? {
        val thumbnail =
            li.selectFirst("img")?.attr("src")?.replaceFirst("/promo/", "/big/") ?: return null
        val url = li.selectFirst("a")?.attr("href") ?: return null
        val name = li.selectFirst("span.sTitle")?.text() ?: return null
        val city = li.selectFirst("a")?.ownText()
        // TODO: Genres
        return Station(url, thumbnail, name, city, null, emptyList())
    }

    private fun parseStationInfo(div: Element): Station? {
        val img = div.selectFirst("img") ?: return null
        val thumbnail = img.attr("src")
        val name = img.attr("alt")
        val url = div.selectFirst("a")?.attr("href") ?: return null
        val city = div.selectFirst(".sMeta_Location")?.ownText()

        val meta =
            div.select(".sMetaTag").mapNotNull { Pair(it.attr("href"), it.ownText()) }

        val category = meta.firstOrNull { "/category/" in it.first }?.let {
            Pair(it.first.split("/").last(), it.second)
        }

        val genres = meta.filter { "/music/" in it.first }.map {
            Pair(it.first.split("/").last(), it.second)
        }

        return Station(
            url = url,
            thumbnail = thumbnail,
            name = name,
            city = city,
            category = category,
            genres = genres
        )
    }

    suspend fun getCategoryStations(category: String): List<Station> =
        getHTML("/category/$category") { document ->
            document.select("#content > .stationEntry").mapNotNull { div ->
                parseStationInfo(div)
            }
        }

    suspend fun getLocationStations(location: String): List<Station> =
        getHTML("/location/$location") { document ->
            document.select("#content > .stationEntry").mapNotNull { div ->
                parseStationInfo(div)
            }
        }


    suspend fun getStream(url: String): String? {
        val res = get(url)
        val body = res.body.string()
        res.close()

        var regex = "mp3: \"(.*?)\"".toRegex()
        var match = regex.find(body)

        if (match != null) {
            val (source) = match.destructured
            return source
        } else {
            regex = "<iframe.*?src=\"(.*?)\".*?>".toRegex()
            match = regex.find(body)

            if (match != null) {
                var (source) = match.destructured
                if (source.startsWith("//")) {
                    source = "https:$source"
                }
                return getStream(source)
            }
        }

        return null
    }


    suspend fun getHomePage(): HomePage =
        getHTML("/") { document ->
            val regions = document.html().let { body ->
                val regex =
                    "<a href=\"https:\\/\\/www\\.e-radio\\.gr\\/location\\/(.*?)\">(.*?)<\\/a>".toRegex()
                regex.findAll(body).map { Pair(it.groupValues[1], it.groupValues[2]) }
                    .distinctBy { it.first }
            }
            val categories =
                document.selectFirst("#tabListenCategoryMenu")?.select("a")?.mapNotNull {
                    Pair(
                        it.attribute("href")?.attributeValue?.split("/")?.last()
                            ?: return@mapNotNull null, it.ownText()
                    )
                } ?: emptyList()
            val radioLists = document.select(".panel").mapNotNull { list ->
                val title = list.selectFirst("h2 > a")?.text() ?: return@mapNotNull null
                val radios =
                    list.select(".homeRadioItem").mapNotNull { li -> parseStation(li) }
                Pair(title, radios)
            }
            HomePage(
                regions = regions.toList(),
                radioLists = radioLists,
                categories = categories
            )
        }
}