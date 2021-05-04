package me.melijn.monitorflux.service.statsposting

import me.melijn.monitorflux.utils.RunnableTask
import me.melijn.monitorflux.objects.BotListApi
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.service.melijn.MelijnStatsInfoService
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
            botListApi.updateBotsOnDiscordXYZ(guildCount) // 2min ratelimit
            botListApi.updateBotlistSpace(guildArray) // 15s ratelimit
            botListApi.updateDiscordBotListCom(guildCount, voice) // no
            botListApi.updateDiscordBotsGG(guildCount, melijnStat.shards.size.toLong()) // 0.05s ratelimit
            botListApi.updateBotsForDiscordCom(guildCount) // no
            botListApi.updateDiscordBoats(guildCount) // 1s

            lastGuilds = guildCount
        }
    }
}