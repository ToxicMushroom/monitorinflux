package me.melijn.monitorflux.objects


import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.OBJECT_MAPPER
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors


class WebManager {

    private val httpClient = OkHttpClient()
        .newBuilder()
        .dispatcher(Dispatcher(Executors.newSingleThreadExecutor()))
        .build()


    fun getResponseFromUrl(
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

        val requestBuilder = Request.Builder()
            .url(fullUrlWithParams)
            .get()

        for ((key, value) in headers) {
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string()
        response.close()

        return responseBody
    }


    fun <T> getObjectFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        obj: Class<T>
    ): T? {
        val response = getResponseFromUrl(url, params, headers)
        return OBJECT_MAPPER.readValue(response, obj)
    }


    fun getJsonNodeFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): JsonNode? {
        val response = getResponseFromUrl(url, params, headers) ?: return null
        return OBJECT_MAPPER.readTree(response)
    }
}