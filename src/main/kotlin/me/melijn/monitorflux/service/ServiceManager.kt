package me.melijn.monitorflux.service

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.melijn.*

class ServiceManager(container: Container, influxDataSource: InfluxDataSource) {

    private var started = false

    private val services: MutableList<Service> = mutableListOf(
        MelijnStatsInfoService(container, influxDataSource),
        MelijnEventsInfoService(container, influxDataSource),
        MelijnTOPGGInfoService(container, influxDataSource),
        MelijnBFDInfoService(container, influxDataSource),
        MelijnDBLComInfoService(container, influxDataSource),
        MelijnDBoatsInfoService(container, influxDataSource),
        MelijnCommandsService(container)
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