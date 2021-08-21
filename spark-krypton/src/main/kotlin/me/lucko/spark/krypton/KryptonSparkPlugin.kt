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
    private val threadDumper = ThreadDumper.GameThread()

    @Listener
    fun onStart(event: ServerStartEvent) {
        platform = SparkPlatform(this)
        platform.enable()
        server.commandManager.register(this, simpleCommandMeta("spark") {})
    }

    @Listener
    fun onStop(event: ServerStopEvent) = platform.disable()

    override fun execute(sender: Sender, args: Array<String>) = platform.executeCommand(KryptonCommandSender(sender), args)

    override fun suggest(sender: Sender, args: Array<String>): List<String> = platform.tabCompleteCommand(KryptonCommandSender(sender), args)

    override fun getCommandName() = "spark"

    override fun getCommandSenders(): Stream<out KryptonCommandSender> = Stream.concat(
        server.players.stream(),
        Stream.of(server.console)
    ).map { KryptonCommandSender(it) }

    override fun executeAsync(task: Runnable) {
        server.scheduler.run(this, task)
    }

    override fun getDefaultThreadDumper(): ThreadDumper = threadDumper.get()

    override fun createTickHook() = KryptonTickHook(this)

    override fun createTickReporter() = KryptonTickReporter(this)

    override fun createClassSourceLookup() = KryptonClassSourceLookup(server.pluginManager)

    override fun getPlatformInfo() = KryptonPlatformInfo(server.platform)

    override fun getPluginDirectory() = folder

    override fun getVersion() = "@version@"

    override fun registerApi(api: Spark) = server.servicesManager.register(this, api)
}
