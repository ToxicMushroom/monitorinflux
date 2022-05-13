package me.melijn.monitorflux.service

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.objects.BotListApi
import me.melijn.monitorflux.service.melijn.MelijnCommandsService
import me.melijn.monitorflux.service.melijn.MelijnEventsInfoService
import me.melijn.monitorflux.service.melijn.MelijnRatelimitingService
import me.melijn.monitorflux.service.melijn.MelijnStatsInfoService
import me.melijn.monitorflux.service.melijn.botlists.MelijnBFDInfoService
import me.melijn.monitorflux.service.melijn.botlists.MelijnDBLComInfoService
import me.melijn.monitorflux.service.melijn.botlists.MelijnTOPGGInfoService
import me.melijn.monitorflux.service.statsposting.StatsService

class ServiceManager(container: Container, influxDataSource: InfluxDataSource) {

    private var started = false

    private val services: MutableList<Service> = mutableListOf(
        MelijnStatsInfoService(container, influxDataSource),
        MelijnEventsInfoService(container, influxDataSource),
        MelijnTOPGGInfoService(container, influxDataSource),
        MelijnBFDInfoService(container, influxDataSource),
        MelijnDBLComInfoService(container, influxDataSource),
        MelijnRatelimitingService(container, influxDataSource),
        MelijnCommandsService(container),
        StatsService(BotListApi(container.webManager.httpClient, container.settings)),
    )

    fun startServices() {
        services.forEach { service ->
            service.start()
        }
        started = true
    }

    fun stopServices() {
        require(started) { "Never started!" }
        services.forEach { service ->
            service.stop()
        }
    }
}