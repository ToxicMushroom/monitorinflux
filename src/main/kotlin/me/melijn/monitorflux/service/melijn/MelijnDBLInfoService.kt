package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.WebManager
import me.melijn.monitorflux.service.Service
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

class MelijnDBLInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_votes", 60, 2, TimeUnit.SECONDS) {

    private val web = WebManager()
    private val botApi = container.settings.botApi
    override val service: Runnable = Runnable {
        try {
            val jsonNode: JsonNode? = web.getJsonNodeFromUrl(
                "https://top.gg/api/bots/${container.settings.botApi.id}",
                headers = mapOf(Pair("Authorization", container.settings.tokens.dblToken))
            )
            if (jsonNode == null) {
                logger.warn("Failed to get dbl/melijn info")
                return@Runnable
            }

            val votes = jsonNode.get("monthlyPoints").intValue()
            val totalVotes = jsonNode.get("points").intValue()
            val pointBuilder = Point
                .measurement("Bot")
                .tag("name", botApi.name)
                .tag("id", botApi.id.toString())

            influxDataSource.writePoint(
                pointBuilder
                    .addField("monthly_points", votes)
                    .addField("total_points", totalVotes)
                    .build()
            )
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}