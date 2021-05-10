package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.melijn.monitorflux.utils.RunnableTask
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.data.MelijnStat
import me.melijn.monitorflux.data.Shard
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.Service
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point

val objectMapper = jacksonObjectMapper()

class MelijnStatsInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_stats", 5, 2) {

    companion object {
        var melijnStat: MelijnStat? = null
    }

    private val botApi = container.settings.botApi
    private val baseUrl = botApi.host

    private val statusList = mutableListOf(
        "connected",
        "logging_in",
        "identifying_session",
        "waiting_to_reconnect",
        "loading_subsystems",
        "attempting_to_reconnect",
        "awaiting_login_confirmation", // if confirm appears again just add it aswell
        "reconnect_queued",
        "attempting_reconnect",
        "connecting_to_websocket",
        "disconnected",
        "shutdown"
    )

    override val service = RunnableTask {
        val temp: MelijnStat? = container.webManager.getJsonObjectFromUrl(
            "$baseUrl/publicStats"
        )
        if (temp == null) {
            logger.warn("Failed to get melijn /publicStats")
            influxDataSource.writePoint(
                Point.measurement("Bot")
                    .addField("uptime_seconds", 0)
                    .addField("jvm_uptime_seconds", 0)
                    .build()
            )
            return@RunnableTask
        }
        melijnStat = temp

        val serverStat = temp.server
        val botStat = temp.bot

        influxDataSource.writePoint(
            Point.measurement("Bot")
                .tag("name", botApi.name)
                .tag("id", botApi.id.toString())
                .addField("ram_total", serverStat.ramTotal)
                .addField("ram_usage", serverStat.ramUsage)
                .addField("uptime_seconds", serverStat.uptime / 1000)
                .addField("jvm_uptime_seconds", botStat.uptime / 1000)
                .addField("jvm_ram_total", botStat.ramTotal)
                .addField("jvm_ram_usage", botStat.ramUsage)
                .addField("cpu_usage", botStat.cpuUsage)
                .addField("melijn_threads", botStat.melijnThreads)
                .addField("all_threads", botStat.jvmThreads)
                .build()
        )


        // Shards
        val shardList = temp.shards
        val valueInfoList = mutableListOf(
            ValueInfo("ping") { shard -> shard.ping },
            ValueInfo("cvcs") { shard -> shard.connectedVoiceChannels },
            ValueInfo("lvcs") { shard -> shard.listeningVoiceChannels },
            ValueInfo("musicplayers") { shard -> shard.musicPlayers },
            ValueInfo("queuedtracks") { shard -> shard.queuedTracks }
        )

        val maxValues = mutableMapOf<String, ShardValue>()
        val minValues = mutableMapOf<String, ShardValue>()

        val shardSize = shardList.size
        var guilds = 0
        var users = 0
        var pingMean = 0
        var cvcs = 0
        var lvcs = 0
        var musicPlayers = 0
        var queuedTracks = 0

        val map = mutableMapOf<String, Int>()
        var unavailable = 0

        for (shard in shardList) {
            computeComparingValues(shard, valueInfoList, maxValues) { curr, new -> new > curr }
            computeComparingValues(shard, valueInfoList, minValues) { curr, new -> new < curr }

            unavailable += shard.unavailable
            guilds += shard.guildCount
            users += shard.userCount
            pingMean += shard.ping
            cvcs += shard.connectedVoiceChannels
            lvcs += shard.listeningVoiceChannels
            musicPlayers += shard.musicPlayers
            queuedTracks += shard.queuedTracks
            map[shard.status.lowercase()] = map.getOrDefault(shard.status.lowercase(), 0) + 1
        }

        for (status in statusList) {
            map.putIfAbsent(status, 0)
        }

        val batch = BatchPoints.builder()
        map.forEach { (eventName, count) ->
            val event: Point = Point.measurement("shards")
                .tag("name", eventName)
                .addField("count", count)
                .build()
            batch.point(event)
        }
        influxDataSource.writeBatch(batch.build())

        pingMean /= shardSize

        val pointBuilder = Point.measurement("Bot")
            .tag("name", botApi.name)
            .tag("id", botApi.id.toString())

        for (minValue in minValues) {
            pointBuilder.addField("min_${minValue.key}_value", minValue.value.value)
            pointBuilder.addField("min_${minValue.key}_shard", minValue.value.shardId)
        }
        for (maxValue in maxValues) {
            pointBuilder.addField("max_${maxValue.key}_value", maxValue.value.value)
            pointBuilder.addField("max_${maxValue.key}_shard", maxValue.value.shardId)
        }

        influxDataSource.writePoint(
            pointBuilder
                .addField("guilds", guilds)
                .addField("unavailable_guilds", unavailable)
                .addField("users", users)
                .addField("ping", pingMean)
                .addField("cvcs", cvcs)
                .addField("lvcs", lvcs)
                .addField("musicplayers", musicPlayers)
                .addField("queuedtracks", queuedTracks)
                .build()
        )
    }

    private fun computeComparingValues(
        shard: Shard, valueInfoList: List<ValueInfo>,
        minValues: MutableMap<String, ShardValue>,
        replacePredicate: (curr: Int, new: Int) -> Boolean
    ) {
        for (valueInfo in valueInfoList) {
            val currMin = minValues[valueInfo.key]?.value
            val shardValue = valueInfo.computeField(shard)
            if (currMin == null || replacePredicate(currMin, shardValue)) {
                minValues[valueInfo.key] = ShardValue(shard.id, shardValue)
            }
        }
    }

    data class ValueInfo(val key: String, val computeField: (Shard) -> Int)
    data class ShardValue(val shardId: Int, val value: Int)
}