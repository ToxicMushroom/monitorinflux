package me.melijn.monitorflux.data

data class MelijnEvents(
    var events: String,
    var commandUses: String,
    var highestGuilds: String,
    var lastPoint: Long
)