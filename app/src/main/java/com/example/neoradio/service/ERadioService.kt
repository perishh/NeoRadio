package com.example.neoradio.service

import com.example.neoradio.model.Station
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parseInputStream
import okhttp3.OkHttpClient
import okhttp3.Request

typealias RadioList = Pair<String, List<Station>>

object ERadioService {
    private const val baseUrl = "https://www.e-radio.gr"
    private val client = OkHttpClient.Builder()
        .build()

    private fun get(endpoint: String) =
        client.newCall(
            Request.Builder().url(baseUrl + endpoint)
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
        val thumbnail = li.selectFirst("img")?.attr("src") ?: return null
        val id =
            li.selectFirst("a")?.attr("href")?.split("www.e-radio.gr/")?.getOrNull(1)?.split("/")
                ?.getOrNull(0) ?: return null
        val name = li.selectFirst("span.sTitle")?.text() ?: return null
        val city = li.selectFirst("a")?.ownText()
        return Station(id, thumbnail, name, city)
    }

    fun getStream(id: String): String? {
        val res = get("/$id/live")
        return res.body.string().split("mp3: \"").getOrNull(1)?.split("\"")?.getOrNull(0).also {
            res.close()
        }
    }


    suspend fun getFeatured(): List<RadioList> =
        getHTML("/") { document ->
            document.select(".panel").mapNotNull { list ->
                val title = list.selectFirst("h2 > a")?.text() ?: return@mapNotNull null
                val radios =
                    list.select(".homeRadioItem").mapNotNull { li -> parseStation(li) }
                Pair(title, radios)
            }
        }
}