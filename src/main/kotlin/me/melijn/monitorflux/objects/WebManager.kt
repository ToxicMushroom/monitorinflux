package me.melijn.monitorflux.objects


import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import me.melijn.monitorflux.OBJECT_MAPPER

class WebManager {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val httpClient = HttpClient(OkHttp) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = JacksonSerializer(objectMapper)
        }
        install(UserAgent) {
            agent = "Melijn / 1.0.0 Influxmonitor"
        }
    }

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


    suspend inline fun <reified T> getJsonObjectFromUrl(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T? {
        val response = getResponseFromUrl(url, params, headers) ?: return null
        return OBJECT_MAPPER.readValue<T>(response)
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