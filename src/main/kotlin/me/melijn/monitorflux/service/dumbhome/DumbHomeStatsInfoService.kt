package me.melijn.monitorflux.service.dumbhome

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.data.DumbHomeStat
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.WebManager
import me.melijn.monitorflux.service.Service
import org.influxdb.dto.Point

class DumbHomeStatsInfoService(
    container: Container,
    influxDataSource: InfluxDataSource
) : Service("dumbhome_stats", 5, 2) {

    private val web = WebManager()
    private val dumbHomeApi = container.settings.dumbHomeApi

    override val service: Runnable = Runnable {
        try {
            val dumbHomeStat: DumbHomeStat? =
                web.getObjectFromUrl("http://${dumbHomeApi.host}:${dumbHomeApi.port}/stats", obj = DumbHomeStat::class.java)
            if (dumbHomeStat == null) {
                logger.warn("Failed to get dumbhome /stats")
                influxDataSource.writePoint(
                    Point
                        .measurement("DumbHome")
                        .addField("uptime_seconds", 0)
                        .addField("jvm_uptime_seconds", 0)
                        .build()
                )
                return@Runnable
            }


            influxDataSource.writePoint(
                Point
                    .measurement("DumbHome")
                    .addField("ram_total", dumbHomeStat.ramTotal)
                    .addField("ram_usage", dumbHomeStat.ramUsage)
                    .addField("uptime_seconds", dumbHomeStat.uptime / 1000)
                    .addField("jvm_uptime_seconds", dumbHomeStat.jvmUptime / 1000)
                    .addField("jvm_ram_total", dumbHomeStat.jvmramTotal)
                    .addField("jvm_ram_usage", dumbHomeStat.jvmramUsage)
                    .addField("cpu_usage", dumbHomeStat.cpuUsage)
                    .addField("dumbhome_threads", dumbHomeStat.dumbhomeThreads)
                    .addField("all_threads", dumbHomeStat.jvmThreads)
                    .build()
            )
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}