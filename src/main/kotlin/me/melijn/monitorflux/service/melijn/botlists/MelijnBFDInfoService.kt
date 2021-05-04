package me.melijn.monitorflux.service.melijn.botlists

import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

// https://botsfordiscord.com/api/bot/:id/votes
class MelijnBFDInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_bfd_votes", 60, 2, TimeUnit.SECONDS) {

    private val botApi = container.settings.botApi
    override val service = RunnableTask {
        val jsonNode: JsonNode? = container.webManager.getJsonNodeFromUrl(
            "https://botsfordiscord.com/api/bot/${container.settings.botApi.id}/votes",
            headers = mapOf(Pair("Authorization", container.settings.tokens.bfdToken))
        )
        if (jsonNode == null) {
            logger.warn("Failed to get bfd/melijn info")
            return@RunnableTask
        }

        val votes = jsonNode.get("votesMonth").intValue()
        val totalVotes = jsonNode.get("votes").intValue()
        val pointBuilder = Point
            .measurement("Bot")
            .tag("name", botApi.name)
            .tag("id", botApi.id.toString())

        influxDataSource.writePoint(
            pointBuilder
                .addField("monthly_bfd_points", votes)
                .addField("total_bfd_points", totalVotes)
                .build()
        )
    }
}