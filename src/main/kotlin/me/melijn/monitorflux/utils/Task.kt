package me.melijn.monitorflux.utils

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class RunnableTask(private val func: suspend () -> Unit) : Runnable {

    private val logger = LoggerFactory.getLogger(RunnableTask::class.java)

    override fun run() {
        runBlocking {
            try {
                func()
            } catch (e: Throwable) {
                logger.error(e)
            }
        }
    }
}
