package me.melijn.monitorflux.service.melijn

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.data.MelijnStat
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask
import org.influxdb.dto.Point

class MelijnStatsInfoService(container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_stats", 5, 2) {
    private val botApi = container.settings.botApi
    private val baseUrl = botApi.host

    override val service = RunnableTask {
        val melijnStat: MelijnStat? = container.webManager.getObjectFromUrl("$baseUrl/stats", obj = MelijnStat::class.java)
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


        influxDataSource.writePoint(
            Point
                .measurement("Bot")
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
    }
}