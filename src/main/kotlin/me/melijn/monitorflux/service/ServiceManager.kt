package me.melijn.monitorflux.service

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.melijn.MelijnBFDInfoService
import me.melijn.monitorflux.service.melijn.MelijnStatsInfoService
import me.melijn.monitorflux.service.melijn.MelijnTOPGGInfoService

class ServiceManager(container: Container, influxDataSource: InfluxDataSource) {

    private var started = false

    private val services: MutableList<Service> = mutableListOf(
        MelijnStatsInfoService(container, influxDataSource),
        MelijnTOPGGInfoService(container, influxDataSource),
        MelijnBFDInfoService(container, influxDataSource)
//        DumbHomeStatsInfoService(container, influxDataSource)
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