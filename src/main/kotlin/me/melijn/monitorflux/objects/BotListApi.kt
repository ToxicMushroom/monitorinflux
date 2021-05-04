package me.melijn.monitorflux.objects

import com.fasterxml.jackson.databind.node.ArrayNode
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.TimeoutCancellationException
import me.melijn.monitorflux.utils.TaskManager
import me.melijn.monitorflux.service.melijn.objectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val TOP_GG_URL = "https://top.gg"
const val BOTS_ON_DISCORD_XYZ_URL = "https://bots.ondiscord.xyz"
const val BOTLIST_SPACE = "https://api.botlist.space"
const val DISCORD_BOT_LIST_COM = "https://discordbotlist.com"
const val DISCORD_BOTS_GG = "https://discord.bots.gg"
const val BOTS_FOR_DISCORD_COM = "https://botsfordiscord.com"
const val DISCORD_BOATS = "https://discord.boats"

class BotListApi(private val httpClient: HttpClient, val settings: Settings) {

    private val logger: Logger = LoggerFactory.getLogger(BotListApi::class.java)

    fun updateTopDotGG(serversArray: List<Long>) {
        val token = settings.tokens.topDotGG
        val url = "$TOP_GG_URL/api/bots/${settings.botApi.id}/stats"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {
            val arr = createJsonArr(serversArray)
            val body = objectMapper.createObjectNode()
            body.set<ArrayNode>("shards", arr)

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body.toString(), ContentType.Application.Json)
            }
        }
    }

    private fun createJsonArr(serversArray: List<Long>): ArrayNode {
        val arr = objectMapper.createArrayNode()
        serversArray.forEach { arr.add(it) }
        return arr
    }

    private suspend fun postBotStats(url: String, builder: HttpRequestBuilder.() -> Unit) {
        try {
            httpClient.post<String>(url, builder)
        } catch (t: TimeoutCancellationException) {
            logger.warn("Failed to post bot stats to: $url | timeoutcancellation")
        } catch (t: ClientRequestException) {
            logger.warn("Failed to post bot stats to: $url | clientrequest")
        } catch (t: ServerResponseException) {
            logger.warn("Failed to post bot stats to: $url | serverresponse")
        } catch (t: SocketTimeoutException) {
            logger.warn("Failed to post bot stats to: $url | sockettimeout")
        }
    }

    fun updateBotsOnDiscordXYZ(servers: Long) {
        val token = settings.tokens.botsOnDiscordXYZ
        val url = "$BOTS_ON_DISCORD_XYZ_URL/bot-api/bots/${settings.botApi.id}/guilds"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {
            val body = objectMapper.createObjectNode()
                .put("guildCount", "$servers")
                .toString()

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body, ContentType.Application.Json)
            }
        }
    }

    fun updateBotlistSpace(serversArray: List<Long>) {
        val token = settings.tokens.botlistSpace
        val url = "$BOTLIST_SPACE/v1/bots/${settings.botApi.id}"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {
            val arr = createJsonArr(serversArray)
            val body = objectMapper.createObjectNode()
                .set<ArrayNode>("shards", arr)
                .toString()

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body, ContentType.Application.Json)
            }
        }
    }

    fun updateDiscordBotListCom(servers: Long, voice: Long) {
        val token = settings.tokens.discordBotListCom
        val url = "$DISCORD_BOT_LIST_COM/api/v1/bots/${settings.botApi.id}/stats"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {

            val body = objectMapper.createObjectNode()
                .put("guilds", servers)
                .put("voice_connections", voice)
                .toString()

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body, ContentType.Application.Json)
            }
            // Might break due to missing content-type header
        }
    }

    fun updateDiscordBotsGG(servers: Long, shards: Long) {
        val token = settings.tokens.discordBotsGG
        val url = "$DISCORD_BOTS_GG/api/v1/bots/${settings.botApi.id}/stats"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {
            val body = objectMapper.createObjectNode()
                .put("guildCount", servers)
                .put("shardCount", shards)
                .toString()

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body, ContentType.Application.Json)
            }
        }
    }

    fun updateBotsForDiscordCom(servers: Long) {
        val token = settings.tokens.botsForDiscordCom
        val url = "$BOTS_FOR_DISCORD_COM/api/bot/${settings.botApi.id}"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {
            val body = objectMapper.createObjectNode()
                .put("server_count", servers)
                .toString()

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body, ContentType.Application.Json)
            }
        }
    }

    fun updateDiscordBoats(servers: Long) {
        val token = settings.tokens.discordBoats
        val url = "$DISCORD_BOATS/api/bot/${settings.botApi.id}"
        if (token.isBlank()) return
        TaskManager.asyncIgnoreEx {
            val body = objectMapper.createObjectNode()
                .put("server_count", servers)
                .toString()

            postBotStats(url) {
                header("Authorization", token)
                this.body = TextContent(body, ContentType.Application.Json)
            }
        }
    }
}