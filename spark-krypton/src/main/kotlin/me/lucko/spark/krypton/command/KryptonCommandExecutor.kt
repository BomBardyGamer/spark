package me.lucko.spark.krypton.command

import me.lucko.spark.krypton.KryptonCommandSender
import me.lucko.spark.krypton.KryptonSparkPlugin
import org.kryptonmc.krypton.api.command.Command
import org.kryptonmc.krypton.api.command.Sender

class KryptonCommandExecutor(private val plugin: KryptonSparkPlugin) : Command("spark") {

    override fun execute(sender: Sender, args: List<String>) {
        plugin.threadDumper.ensureSetup()
        plugin.platform.executeCommand(KryptonCommandSender(sender), args.toTypedArray())
    }

    override fun suggest(sender: Sender, args: List<String>): List<String> =
        plugin.platform.tabCompleteCommand(KryptonCommandSender(sender), args.toTypedArray())
}