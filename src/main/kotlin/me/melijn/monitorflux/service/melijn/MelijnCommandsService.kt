package me.melijn.monitorflux.service.melijn

import com.fasterxml.jackson.databind.JsonNode
import me.melijn.monitorflux.Container
import me.melijn.monitorflux.service.Service
import me.melijn.monitorflux.utils.RunnableTask

class MelijnCommandsService(container: Container) : Service("melijn_commands", 600, 0) {

    private val botApi = container.settings.botApi
    private val baseUrl = botApi.host

    companion object {
        val map = mutableMapOf<Int, String>()
    }

    override val service: RunnableTask = RunnableTask {
        val commands: JsonNode = container.webManager.getJsonNodeFromUrl(
            "$baseUrl/publicStats"
        ) ?: return@RunnableTask
        for (index in 0..commands.size()) {
            val cmd = commands[index]
            println(cmd.toString())
        }
    }
}