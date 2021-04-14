package me.melijn.monitorflux.service.melijn

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
        val commands: Map<Int, String> = container.webManager.getObjectFromUrl(
            "$baseUrl/commandMap"
        ) ?: return@RunnableTask

        map = commands
    }
}