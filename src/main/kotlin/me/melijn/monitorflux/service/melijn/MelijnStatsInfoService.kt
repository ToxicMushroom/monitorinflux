package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.data.MelijnStat
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.Point


val objectMapper = jacksonObjectMapper()

class MelijnStatsInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_stats", 5, 2) {
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
        "disconnected"
    )

    override val service = RunnableTask {
        val melijnStat: MelijnStat? = container.webManager.getObjectFromUrl(
            "$baseUrl/stats",
            obj = MelijnStat::class.java
        )
        if (melijnStat == null) {
            logger.warn("Failed to get melijn /stats")
            influxDataSource.writePoint(
                Point
                    .measurement("Bot")
                    .addField("uptime_seconds", 0)
                    .addField("jvm_uptime_seconds", 0)
                    .build()
            )
            return@RunnableTask
        }

        val serverStat = melijnStat.server

        val botStat = melijnStat.bot
        val point = Point
            .measurement("Bot")


        influxDataSource.writePoint(
            point
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
        val shardList = melijnStat.shards
        val pointBuilder = Point
            .measurement("Bot")
            .tag("name", botApi.name)
            .tag("id", botApi.id.toString())

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
            pointBuilder
                .addField("${shard.id}_ping", shard.ping)
                .addField("${shard.id}_cvcs", shard.connectedVoiceChannels)
                .addField("${shard.id}_lvcs", shard.listeningVoiceChannels)
                .addField("${shard.id}_musicplayers", shard.musicPlayers)
                .addField("${shard.id}_queuedtracks", shard.queuedTracks)

            unavailable += shard.unavailable
            guilds += shard.guildCount
            users += shard.userCount
            pingMean += shard.ping
            cvcs += shard.connectedVoiceChannels
            lvcs += shard.listeningVoiceChannels
            musicPlayers += shard.musicPlayers
            queuedTracks += shard.queuedTracks
            map[shard.status.toLowerCase()] = map.getOrDefault(shard.status.toLowerCase(), 0) + 1
        }

        for (s in statusList) {
            map.putIfAbsent(s, 0)
        }

        map.forEach { (eventName, count) ->
            val event: Point = Point.measurement("shards")
                .tag("name", eventName)
                .addField("count", count)
                .build()
            influxDataSource.writePoint(event)
        }

        objectMapper
            .readValue<Map<String, Int>>(melijnStat.events)
            .forEach { (eventName, count) ->
                val event: Point = Point.measurement("events")
                    .tag("name", eventName)
                    .addField("count", count / ((System.currentTimeMillis() - melijnStat.lastPoint) / 1000.0))
                    .build()
                influxDataSource.writePoint(event)
            }

        pingMean /= shardSize

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
}