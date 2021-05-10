package me.melijn.monitorflux.objects

import io.github.cdimascio.dotenv.dotenv

data class Settings(
    val database: Database,
    val botApi: BotApi,
    val tokens: Tokens
) {
    data class Database(
        var db: String,
        var password: String,
        var user: String,
        var host: String,
        var port: Int
    )

    data class BotApi(
        var host: String,
        var name: String,
        var id: Long
    )

    data class Tokens(
        var topDotGG: String,
        var botsOnDiscordXYZ: String,
        var botlistSpace: String,
        var discordBotListCom: String,
        var discordBotsGG: String,
        var botsForDiscordCom: String,
        var discordBoats: String
    )

    companion object {
        private val dotenv = dotenv {
            this.filename = System.getenv("ENV_FILE") ?: ".env"
            this.ignoreIfMissing = true
        }

        fun get(path: String): String = dotenv[path.uppercase().replace(".", "_")]
            ?: throw IllegalStateException("missing env value: $path")

        fun getLong(path: String): Long = get(path).toLong()
        fun getInt(path: String): Int = get(path).toInt()
        fun getBoolean(path: String): Boolean = get(path).toBoolean()

        fun initSettings(): Settings {

            return Settings(
                Database(
                    get("database.database"),
                    get("database.password"),
                    get("database.user"),
                    get("database.host"),
                    getInt("database.port")
                ),
                BotApi(
                    get("botapi.host"),
                    get("botapi.name"),
                    getLong("botapi.id")
                ),
                Tokens(
                    get("token.topDotGG"),
                    get("token.botsOnDiscordXYZ"),
                    get("token.botListSpace"),
                    get("token.discordBotListCom"),
                    get("token.discordBotsGG"),
                    get("token.botsForDiscordCom"),
                    get("token.discordBoats")
                )
            )
        }
    }
}