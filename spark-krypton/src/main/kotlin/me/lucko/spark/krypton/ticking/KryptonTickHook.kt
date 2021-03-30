package me.lucko.spark.krypton.ticking

import me.lucko.spark.common.tick.AbstractTickHook
import me.lucko.spark.krypton.KryptonSparkPlugin
import org.kryptonmc.krypton.api.event.Listener
import org.kryptonmc.krypton.api.event.events.ticking.TickStartEvent

class KryptonTickHook(private val plugin: KryptonSparkPlugin) : AbstractTickHook() {

    @Listener
    fun onTickStart(event: TickStartEvent) = onTick()

    override fun start() = plugin.registerListener(this)

    override fun close() = Unit
}