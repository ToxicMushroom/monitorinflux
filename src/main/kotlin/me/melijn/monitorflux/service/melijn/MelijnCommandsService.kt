package me.melijn.monitorflux.service.melijn

import io.ktor.client.request.*
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask

class MelijnCommandsService(container: Container) : Service("melijn_commands", 600, 0) {

    private val botApi = container.settings.botApi
    private val baseUrl = botApi.host

    companion object {
        var map = mapOf<Int, String>()
    }

    override val service: RunnableTask = RunnableTask {
        val commands: Map<Int, String> = try {
            container.webManager.httpClient.get(
                "$baseUrl/commandMap"
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        } ?: return@RunnableTask

        map = commands
    }
}