package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.module.kotlin.readValue
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.data.MelijnEvents
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point

class MelijnEventsInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_events", 60, 30) {

    private val botApi = container.settings.botApi
    private val baseUrl = botApi.host

    override val service = RunnableTask {
        val melijnStat: MelijnEvents? = container.webManager.getObjectFromUrl(
            "$baseUrl/events"
        )
        if (melijnStat == null) {
            logger.warn("Failed to get melijn /events")
            return@RunnableTask
        }

        val batchBuilder = BatchPoints.builder()

        objectMapper.readValue<Map<String, Int>>(melijnStat.events)
            .forEach { (eventName, count) ->
                val event: Point = Point.measurement("events")
                    .tag("name", eventName)
                    .addField("count", count / ((System.currentTimeMillis() - melijnStat.lastPoint) / 1000.0))
                    .build()
                batchBuilder.point(event)
            }

        objectMapper.readValue<Map<Int, Int>>(melijnStat.commandUses)
            .forEach { (cmdId, uses) ->
                val event: Point = Point.measurement("command_uses")
                    .tag("name", parseName(cmdId))
                    .addField("count", uses)
                    .build()
                batchBuilder.point(event)
            }

        objectMapper.readValue<List<Pair<Long, Int>>>(melijnStat.highestGuilds)
            .forEach { (id, uses) ->
                val event: Point = Point.measurement("top_10_active_entities")
                    .tag("name", id.toString())
                    .addField("count", uses)
                    .build()
                batchBuilder.point(event)
            }

        val points = batchBuilder.build()
        influxDataSource.writeBatch(points)
    }

    private fun parseName(cmdId: Int): String {
        return MelijnCommandsService.map[cmdId] ?: "$cmdId"
    }
}