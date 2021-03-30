package me.lucko.spark.krypton.ticking

import me.lucko.spark.common.tick.AbstractTickReporter
import me.lucko.spark.krypton.KryptonSparkPlugin
import org.kryptonmc.krypton.api.event.Listener
import org.kryptonmc.krypton.api.event.events.ticking.TickEndEvent

class KryptonTickReporter(private val plugin: KryptonSparkPlugin) : AbstractTickReporter() {

    @Listener
    fun onTickEnd(event: TickEndEvent) = onTick(event.tickDuration.toDouble())

    override fun start() = plugin.registerListener(this)

    override fun close() = Unit
}