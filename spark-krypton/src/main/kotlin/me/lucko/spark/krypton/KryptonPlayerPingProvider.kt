package me.lucko.spark.krypton

import com.google.common.collect.ImmutableMap
import me.lucko.spark.common.monitor.ping.PlayerPingProvider
import org.kryptonmc.api.Server

class KryptonPlayerPingProvider(private val server: Server) : PlayerPingProvider {

    override fun poll(): Map<String, Int> {
        val result = ImmutableMap.builder<String, Int>()
        for (player in server.players) {
            result.put(player.name, player.ping)
        }
        return result.build()
    }
}