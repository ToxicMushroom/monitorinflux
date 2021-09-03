package me.melijn.monitorflux.service.melijn.botlists

import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.DISCORD_BOT_LIST_COM
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

// https://discordbotlist.com/api/v1/bots/:id
class MelijnDBLComInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_dblcom_votes", 60, 2, TimeUnit.SECONDS) {

    private val botApi = container.settings.botApi
    override val service = RunnableTask {
        val jsonNode: JsonNode? = container.webManager.getJsonNodeFromUrl(
            "${DISCORD_BOT_LIST_COM}/api/v1/bots/${container.settings.botApi.id}"
        )
        if (jsonNode == null) {
            logger.warn("Failed to get dblcom/melijn info")
            return@RunnableTask
        }

        val votes = jsonNode.get("metrics")?.get("upvotes")?.intValue() ?: 0
        val pointBuilder = Point
            .measurement("Bot")
            .tag("name", botApi.name)
            .tag("id", botApi.id.toString())

        influxDataSource.writePoint(
            pointBuilder
                .addField("monthly_dblcom_points", votes)
                .build()
        )
    }
}