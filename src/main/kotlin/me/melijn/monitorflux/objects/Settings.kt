package me.melijn.monitorflux.objects

data class Settings(
    val database: Database,
    val botApi: BotApi,
    val dumbHomeApi: DumbHomeApi,
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

    data class DumbHomeApi(
        var host: String,
        var port: Int
    )

    data class Tokens(
        var dblToken: String,
        var bfdToken: String
    )
}