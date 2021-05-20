package me.melijn.monitorflux

import me.melijn.monitorflux.datasource.InfluxDataSource
import me.melijn.monitorflux.service.ServiceManager

class Monitor {

    init {
        val container = Container()
        val influxDataSource = InfluxDataSource(container.influxDB)
        val serviceManager = ServiceManager(container, influxDataSource)
        serviceManager.startServices()
    }
}

fun main(args: Array<String>) {
    Monitor()
}