package me.melijn.monitorflux.objects


import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import me.melijn.monitorflux.OBJECT_MAPPER


class WebManager {

    private val httpClient = HttpClient(CIO)


    private suspend fun getResponseFromUrl(
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

        return httpClient.get<String>(fullUrlWithParams) {
            for ((key, value) in headers) {
                header(key, value)
            }
        }
    }


    suspend fun <T> getObjectFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        obj: Class<T>
    ): T? {
        val response = getResponseFromUrl(url, params, headers)
        return OBJECT_MAPPER.readValue(response, obj)
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