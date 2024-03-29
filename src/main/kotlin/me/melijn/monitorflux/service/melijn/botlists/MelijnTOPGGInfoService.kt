package me.melijn.monitorflux.service.melijn.botlists

import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.TOP_GG_URL
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit


class MelijnTOPGGInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_topgg_votes", 60, 2, TimeUnit.SECONDS) {

    private val botApi = container.settings.botApi
    override val service = RunnableTask {
        val jsonNode: JsonNode? = container.webManager.getJsonNodeFromUrl(
            "${TOP_GG_URL}/api/bots/${container.settings.botApi.id}",
            headers = mapOf(Pair("Authorization", container.settings.tokens.topDotGG))
        )
        if (jsonNode == null) {
            logger.warn("Failed to get dbl/melijn info")
            return@RunnableTask
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
    }
}