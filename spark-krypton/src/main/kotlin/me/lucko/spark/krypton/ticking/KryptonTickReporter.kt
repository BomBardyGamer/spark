package me.lucko.spark.krypton.ticking

import me.lucko.spark.common.tick.AbstractTickReporter
import me.lucko.spark.krypton.KryptonSparkPlugin
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.TickEndEvent

class KryptonTickReporter(private val plugin: KryptonSparkPlugin) : AbstractTickReporter() {

    @Listener
    fun onTickEnd(event: TickEndEvent) = onTick(event.tickDuration.toDouble())

    override fun start() = plugin.server.eventManager.register(plugin, this)

    override fun close() = plugin.server.eventManager.unregisterListener(plugin, this)
}
