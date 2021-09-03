package me.melijn.monitorflux.service.melijn.botlists

import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.DISCORD_BOATS
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

// https://discord.boats/api/bot/:id
class MelijnDBoatsInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_dboats_votes", 60, 2, TimeUnit.SECONDS) {

    private val botApi = container.settings.botApi
    override val service = RunnableTask {
        val jsonNode: JsonNode? = container.webManager.getJsonNodeFromUrl(
            "${DISCORD_BOATS}/api/bot/${container.settings.botApi.id}"
        )
        if (jsonNode == null) {
            logger.warn("Failed to get dboats/melijn info")
            return@RunnableTask
        }

        val votes = jsonNode.get("bot_vote_count").intValue()
        val pointBuilder = Point
            .measurement("Bot")
            .tag("name", botApi.name)
            .tag("id", botApi.id.toString())

        influxDataSource.writePoint(
            pointBuilder
                .addField("monthly_dboats_points", votes)
                .build()
        )
    }
}