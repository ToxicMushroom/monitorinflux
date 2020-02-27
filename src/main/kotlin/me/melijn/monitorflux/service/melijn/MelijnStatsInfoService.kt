package me.melijn.monitorflux.service.melijn

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.data.Stat
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.WebManager
import me.melijn.monitorflux.service.Service
import org.influxdb.dto.Point

class MelijnStatsInfoService (container: Container, private val influxDataSource: InfluxDataSource) :
    Service("melijn_stats", 5, 2) {

    private val web = WebManager()
    private val botApi = container.settings.botApi
    override val service: Runnable = Runnable {
        try {
            val stat: Stat? = web.getObjectFromUrl("http://${botApi.host}:${botApi.port}/stats", obj = Stat::class.java)
            if (stat == null) {
                logger.warn("Failed to get melijn /stats")
                influxDataSource.writePoint(
                    Point
                        .measurement("Bot")
                        .addField("uptime_millis", 0)
                        .addField("jvm_uptime_millis", 0)
                        .build()
                )
                return@Runnable
            }

            val serverStat = stat.server

            val botStat = stat.bot


            influxDataSource.writePoint(
                Point
                    .measurement("Bot")
                    .tag("name", botApi.name)
                    .tag("id", botApi.id.toString())
                    .addField("ram_total", serverStat.ramTotal)
                    .addField("ram_usage", serverStat.ramUsage)
                    .addField("uptime_millis", serverStat.uptime/1000)
                    .addField("jvm_uptime_millis", botStat.uptime/1000)
                    .addField("jvm_ram_total", botStat.ramTotal)
                    .addField("jvm_ram_usage", botStat.ramUsage)
                    .addField("cpu_usage", botStat.cpuUsage)
                    .addField("melijn_threads", botStat.melijnThreads)
                    .addField("all_threads", botStat.jvmThreads)
                    .build()
            )
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}