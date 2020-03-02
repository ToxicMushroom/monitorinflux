package me.melijn.monitorflux.service

import me.melijn.monitorflux.Container
import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.melijn.DBLInfoService
import me.melijn.monitorflux.service.melijn.MelijnShardInfoService
import me.melijn.monitorflux.service.melijn.MelijnStatsInfoService

class ServiceManager(container: Container, influxDataSource: InfluxDataSource) {

    private var started = false

    private val services: MutableList<Service> = mutableListOf(
        MelijnShardInfoService(container, influxDataSource),
        MelijnStatsInfoService(container, influxDataSource),
        DBLInfoService(container, influxDataSource)
    )

    fun startServices() {
        services.forEach { service ->
            service.start()
            service.logger.info("Started ${service.name}Service")
        }
        started = true
    }

    fun stopServices() {
        require(started) { "Never started!" }
        services.forEach { service ->
            service.stop()
            service.logger.info("Stopped ${service.name}Service")
        }
    }
}