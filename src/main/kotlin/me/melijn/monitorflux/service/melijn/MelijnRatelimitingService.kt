package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.module.kotlin.readValue
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

class MelijnRatelimitingService(
    container: Container, private val influxDataSource: InfluxDataSource
) : Service("melijn-ratelimiting", 1, 1, TimeUnit.MINUTES) {

    private val botApi = container.settings.botApi
    private val baseUrl = botApi.host
    override val service: RunnableTask = RunnableTask {
        val melijnStat = container.webManager.getJsonNodeFromUrl(
            "$baseUrl/ratelimit",
            headers = mapOf("Authorization" to container.settings.tokens.melijnBackend)
        )
        if (melijnStat == null) {
            logger.warn("Failed to get melijn /ratelimit")
            return@RunnableTask
        }

        val botCounts = objectMapper.readValue<Map<Int, Int>>(melijnStat["botCounts"].asText())
        val botRouteCounts = objectMapper.readValue<Map<String, MutableMap<Int, Int>>>(melijnStat["botRouteCounts"].asText())

        val batchBuilder = BatchPoints.builder()
        botRouteCounts.forEach { (route, statusses) ->
            statusses.forEach { (status, count) ->
                batchBuilder.point(
                    Point.measurement("ratelimit_path")
                        .tag(route, status.toString())
                        .tag("status", status.toString())
                        .addField("count", count)
                        .build()
                )
            }
        }
        botCounts.forEach { (status, count) ->
            batchBuilder.point(
                Point.measurement("ratelimit_code")
                    .addField("$status", count)
                    .build()
            )
        }

        influxDataSource.writeBatch(batchBuilder.build())
    }
}