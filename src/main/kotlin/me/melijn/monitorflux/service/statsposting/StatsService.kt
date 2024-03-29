package me.melijn.monitorflux.service.statsposting

import me.melijn.monitorflux.objects.BotListApi
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.service.melijn.MelijnStatsInfoService
import me.melijn.monitorflux.utils.RunnableTask
import java.util.concurrent.TimeUnit

class StatsService(
    private val botListApi: BotListApi
) : Service("Stats", 3, 3, TimeUnit.MINUTES) {

    var lastGuilds: Long = 0
    override val service = RunnableTask {
        val melijnStat = MelijnStatsInfoService.melijnStat ?: return@RunnableTask
        val guildArray = melijnStat.shards.map { shard -> shard.guildCount.toLong() }

        var guildCount = 0L
        melijnStat.shards.forEach { guildCount += it.guildCount }

        if (guildCount > lastGuilds) {
            val voice = melijnStat.shards.sumOf { it.listeningVoiceChannels.toLong() }

            botListApi.updateTopDotGG(guildArray) // 1s ratelimit
            botListApi.updateDiscordBotsGG(guildCount, melijnStat.shards.size.toLong()) // 0.05s ratelimit
            botListApi.updateDiscordsCom(guildCount) // no

            lastGuilds = guildCount
        }
    }
}