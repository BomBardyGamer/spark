/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.krypton

import com.google.inject.Inject
import me.lucko.spark.api.Spark
import me.lucko.spark.common.SparkPlatform
import me.lucko.spark.common.SparkPlugin
import me.lucko.spark.common.monitor.ping.PlayerPingProvider
import me.lucko.spark.common.platform.PlatformInfo
import me.lucko.spark.common.platform.world.WorldInfoProvider
import me.lucko.spark.common.sampler.ThreadDumper
import me.lucko.spark.common.sampler.source.ClassSourceLookup
import me.lucko.spark.common.sampler.source.SourceMetadata
import me.lucko.spark.common.tick.TickHook
import me.lucko.spark.common.tick.TickReporter
import org.apache.logging.log4j.Logger
import org.kryptonmc.api.Server
import org.kryptonmc.api.command.CommandMeta
import org.kryptonmc.api.event.Event
import org.kryptonmc.api.event.EventFilter
import org.kryptonmc.api.event.EventNode
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.ServerStartEvent
import org.kryptonmc.api.event.server.ServerStopEvent
import org.kryptonmc.api.event.server.TickEvent
import org.kryptonmc.api.plugin.PluginDescription
import org.kryptonmc.api.plugin.annotation.DataFolder
import org.kryptonmc.api.scheduling.ExecutionType
import java.nio.file.Path
import java.util.logging.Level
import java.util.stream.Stream

class KryptonSparkPlugin @Inject constructor(
    private val server: Server,
    val eventNode: EventNode<Event>,
    private val logger: Logger,
    @DataFolder
    private val folder: Path,
    private val description: PluginDescription
) : SparkPlugin {

    private var platform: SparkPlatform? = null
    private val platformInfo = KryptonPlatformInfo(server.platform)

    private val tickMonitorEventNode: EventNode<TickEvent>

    init {
        val monitorFilter = EventFilter.create<TickEvent, Any>(TickEvent::class.java, null, null)
        tickMonitorEventNode = EventNode.filteredForType("spark_tick_monitor", monitorFilter) { _, _ -> true }
    }

    @Listener
    fun onStart(event: ServerStartEvent) {
        platform = SparkPlatform(this)
        platform!!.enable()

        eventNode.addChild(tickMonitorEventNode)
        server.commandManager.register(KryptonSparkCommand(platform!!), CommandMeta.builder("spark").build())
    }

    @Listener
    fun onStop(event: ServerStopEvent) {
        platform?.disable()
    }

    override fun getVersion(): String = description.version

    override fun getPluginDirectory(): Path = folder

    override fun getCommandName() = "spark"

    override fun getCommandSenders(): Stream<out KryptonCommandSender> {
        return Stream.concat(server.players.stream(), Stream.of(server.console)).map { KryptonCommandSender(it) }
    }

    override fun executeAsync(task: Runnable) {
        server.scheduler.buildTask(task).executionType(ExecutionType.ASYNCHRONOUS).schedule()
    }

    override fun executeSync(task: Runnable) {
        server.scheduler.buildTask(task).executionType(ExecutionType.SYNCHRONOUS).schedule()
    }

    override fun log(level: Level, msg: String) {
        when (level) {
            Level.INFO -> logger.info(msg)
            Level.WARNING -> logger.warn(msg)
            Level.SEVERE -> logger.error(msg)
            else -> throw IllegalArgumentException(level.name)
        }
    }

    // We probably want to include all threads, as we want the tick scheduler, individual tick threads, and the network threads for profiling.
    override fun getDefaultThreadDumper(): ThreadDumper = ThreadDumper.ALL

    override fun createTickHook(): TickHook = KryptonTickHook(tickMonitorEventNode)

    override fun createTickReporter(): TickReporter = KryptonTickReporter(tickMonitorEventNode)

    override fun createClassSourceLookup(): ClassSourceLookup = KryptonClassSourceLookup(server.pluginManager)

    override fun getKnownSources(): Collection<SourceMetadata> {
        return SourceMetadata.gather(
            server.pluginManager.plugins,
            { it.description.id },
            { it.description.version },
            { it.description.authors.joinToString(", ") }
        )
    }

    override fun createPlayerPingProvider(): PlayerPingProvider = KryptonPlayerPingProvider(server)

    override fun createWorldInfoProvider(): WorldInfoProvider = KryptonWorldInfoProvider(server)

    override fun getPlatformInfo(): PlatformInfo = platformInfo

    override fun registerApi(api: Spark) {
        server.servicesManager.register(this, Spark::class.java, api)
    }
}
