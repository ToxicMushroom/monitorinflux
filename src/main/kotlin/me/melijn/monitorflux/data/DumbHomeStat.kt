package me.melijn.monitorflux.data

data class DumbHomeStat(
    val uptime: Long,
    val jvmUptime: Long,
    val dumbhomeThreads: Int,
    val jvmramUsage: Int,
    val jvmramTotal: Int,
    val jvmThreads: Int,
    val cpuUsage: Double,
    val ramUsage: Int,
    val ramTotal: Int
)