package me.melijn.monitorflux


import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.melijn.monitorflux.data.MelijnStat
import me.melijn.monitorflux.objects.Settings
import me.melijn.monitorflux.objects.WebManager
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import java.io.File
import java.util.concurrent.TimeUnit

val OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

class Container {

    var settings: Settings = Settings.initSettings()

    val webManager = WebManager()

    val influxDB: InfluxDB = if (settings.database.user.isEmpty() && settings.database.password.isEmpty()) {
        InfluxDBFactory.connect("http://${settings.database.host}:${settings.database.port}", "", "")
    } else InfluxDBFactory.connect(
        "http://${settings.database.host}:${settings.database.port}",
        settings.database.user,
        settings.database.password
    )

    init {
        val dbSettings = settings.database

        val influx = influxDB
        influx.enableBatch(300, 5, TimeUnit.SECONDS)
        influx.setDatabase(dbSettings.db)
    }
}