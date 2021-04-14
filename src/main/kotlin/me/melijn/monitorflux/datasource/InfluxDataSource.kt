package me.melijn.monitorflux.datasource

import org.influxdb.InfluxDB
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point

class InfluxDataSource(private val influx: InfluxDB) {

    fun writePoint(point: Point) {
        influx.write(point)
    }

    fun writeBatch(points: BatchPoints) {
        influx.write(points)
    }
}