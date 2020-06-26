package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.OBJECT_MAPPER
import me.melijn.monitorflux.data.Shard
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.WebManager
import me.melijn.monitorflux.service.Service
import org.influxdb.dto.Point

class MelijnShardInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_shards", 5, 3) {

    private val web = WebManager()
    private val botApi = container.settings.botApi
    val statusList = mutableListOf<String>()

    override val service: Runnable = Runnable {
        try {
            val jsonNode: JsonNode? = web.getJsonNodeFromUrl("http://${botApi.host}:${botApi.port}/shards")
            if (jsonNode == null) {
                logger.warn("Failed to get melijn /shards")
                return@Runnable
            }
            val shardList: List<Shard> = OBJECT_MAPPER.convertValue(
                jsonNode,
                object : TypeReference<List<Shard>>() {}
            )

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

            for (shard in shardList) {
                pointBuilder
                    .addField("${shard.id}_ping", shard.ping)
                    .addField("${shard.id}_cvcs", shard.connectedVoiceChannels)
                    .addField("${shard.id}_lvcs", shard.listeningVoiceChannels)
                    .addField("${shard.id}_musicplayers", shard.musicPlayers)
                    .addField("${shard.id}_queuedtracks", shard.queuedTracks)

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

            for ((status, amount) in map) {
                if (!statusList.contains(status)) {
                    statusList.add(status)
                }

                pointBuilder.addField("shards_$status", amount)
            }

            pingMean /= shardSize

            influxDataSource.writePoint(
                pointBuilder
                    .addField("guilds", guilds)
                    .addField("users", users)
                    .addField("ping", pingMean)
                    .addField("cvcs", cvcs)
                    .addField("lvcs", lvcs)
                    .addField("musicplayers", musicPlayers)
                    .addField("queuedtracks", queuedTracks)
                    .build()
            )
        } catch (t: Throwable) {
            logger.error(t.message ?: "")

        }
    }
}