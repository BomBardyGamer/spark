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
import me.lucko.spark.common.sampler.ThreadDumper
import me.lucko.spark.krypton.ticking.KryptonTickHook
import me.lucko.spark.krypton.ticking.KryptonTickReporter
import org.kryptonmc.api.Server
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.command.SimpleCommand
import org.kryptonmc.api.command.meta.simpleCommandMeta
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.ServerStartEvent
import org.kryptonmc.api.event.server.ServerStopEvent
import org.kryptonmc.api.plugin.annotation.DataFolder
import org.kryptonmc.api.plugin.annotation.Plugin
import org.kryptonmc.api.service.register
import java.nio.file.Path
import java.util.stream.Stream

@Plugin("spark", "spark", "@version@", "@desc@", ["Luck"])
class KryptonSparkPlugin @Inject constructor(
    val server: Server,
    @DataFolder private val folder: Path
) : SparkPlugin, SimpleCommand {

    private lateinit var platform: SparkPlatform
    private val platformInfo = KryptonPlatformInfo(server.platform)
    private val threadDumper = ThreadDumper.GameThread()

    @Listener
    fun onStart(event: ServerStartEvent) {
        platform = SparkPlatform(this)
        platform.enable()
        server.commandManager.register(this, simpleCommandMeta("spark") {})
    }

    @Listener
    fun onStop(event: ServerStopEvent) = platform.disable()

    override fun execute(sender: Sender, args: Array<String>) {
        platform.executeCommand(KryptonCommandSender(sender), args)
    }

    override fun suggest(sender: Sender, args: Array<String>): List<String> =
        platform.tabCompleteCommand(KryptonCommandSender(sender), args)

    override fun getCommandName() = "spark"

    override fun getCommandSenders(): Stream<out KryptonCommandSender> = Stream.concat(
        server.players.stream(),
        Stream.of(server.console)
    ).map { KryptonCommandSender(it) }

    override fun executeAsync(task: Runnable) {
        server.scheduler.run(this) { task.run() }
    }

    override fun getDefaultThreadDumper(): ThreadDumper = threadDumper.get()

    override fun createTickHook() = KryptonTickHook(this)

    override fun createTickReporter() = KryptonTickReporter(this)

    override fun createClassSourceLookup() = KryptonClassSourceLookup(server.pluginManager)

    override fun getPlatformInfo() = platformInfo

    override fun getPluginDirectory() = folder

    override fun getVersion() = "@version@"

    override fun registerApi(api: Spark) = server.servicesManager.register(this, api)
}
