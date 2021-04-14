package me.melijn.monitorflux.data

data class MelijnEvents(
    var events: String,
    var commandUses: String,
    var entityUses: String,
    var highestEntities: String,
    var lastPoint: Long
)