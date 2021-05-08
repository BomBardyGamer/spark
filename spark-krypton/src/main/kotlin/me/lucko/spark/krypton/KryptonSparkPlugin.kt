package me.lucko.spark.krypton

import me.lucko.spark.api.Spark
import me.lucko.spark.common.SparkPlatform
import me.lucko.spark.common.SparkPlugin
import me.lucko.spark.common.command.sender.CommandSender
import me.lucko.spark.common.sampler.ThreadDumper
import me.lucko.spark.common.tick.TickHook
import me.lucko.spark.common.tick.TickReporter
import me.lucko.spark.krypton.command.KryptonCommandExecutor
import me.lucko.spark.krypton.ticking.KryptonTickHook
import me.lucko.spark.krypton.ticking.KryptonTickReporter
import org.kryptonmc.krypton.api.plugin.Plugin
import org.kryptonmc.krypton.api.plugin.PluginContext
import org.kryptonmc.krypton.api.service.register
import java.nio.file.Path
import java.util.stream.Stream

class KryptonSparkPlugin(context: PluginContext) : Plugin(context), SparkPlugin {

    val platform = SparkPlatform(this)
    val threadDumper = ThreadDumper.GameThread()

    init {
        registerCommand(KryptonCommandExecutor(this))
    }

    override fun initialize() = platform.enable()

    override fun shutdown() = platform.disable()

    override fun getCommandName() = "spark"

    override fun getCommandSenders(): Stream<out KryptonCommandSender> = Stream.concat(
        context.server.players.stream(),
        Stream.of(context.server.console)
    ).map { KryptonCommandSender(it) }

    override fun executeAsync(task: Runnable) {
        context.server.scheduler.run(this) { task.run() }
    }

    override fun getDefaultThreadDumper(): ThreadDumper = threadDumper.get()

    override fun createTickHook() = KryptonTickHook(this)

    override fun createTickReporter() = KryptonTickReporter(this)

    override fun getPlatformInfo() = KryptonPlatformInfo(context.server)

    override fun getPluginDirectory() = context.folder

    override fun getVersion() = context.description.version

    override fun registerApi(api: Spark) = context.server.servicesManager.register(this, api)
}