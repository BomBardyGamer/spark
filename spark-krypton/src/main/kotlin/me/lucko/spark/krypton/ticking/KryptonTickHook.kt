package me.lucko.spark.krypton.ticking

import me.lucko.spark.common.tick.AbstractTickHook
import me.lucko.spark.krypton.KryptonSparkPlugin
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.TickStartEvent

class KryptonTickHook(private val plugin: KryptonSparkPlugin) : AbstractTickHook() {

    @Listener
    fun onTickStart(event: TickStartEvent) = onTick()

    override fun start() = plugin.server.eventManager.register(plugin, this)

    override fun close() = plugin.server.eventManager.unregisterListener(plugin, this)
}
