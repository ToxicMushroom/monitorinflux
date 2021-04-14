package me.melijn.monitorflux.objects


import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import me.melijn.monitorflux.OBJECT_MAPPER


class WebManager {

    private val httpClient = HttpClient(OkHttp)

    suspend fun getResponseFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String>
    ): String? {
        val fullUrlWithParams = if (params.isEmpty()) {
            url
        } else {
            url + params.entries.joinToString("&", "?",
                transform = { entry ->
                    entry.key + "=" + entry.value
                }
            )
        }

        return try {
            httpClient.get<String>(fullUrlWithParams) {
                for ((key, value) in headers) {
                    header(key, value)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }


    suspend inline fun <reified T> getObjectFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T? {
        val response = getResponseFromUrl(url, params, headers)
        return OBJECT_MAPPER.readValue(response, T::class.java)
    }


    suspend fun getJsonNodeFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): JsonNode? {
        val response = getResponseFromUrl(url, params, headers) ?: return null
        return OBJECT_MAPPER.readTree(response)
    }
}