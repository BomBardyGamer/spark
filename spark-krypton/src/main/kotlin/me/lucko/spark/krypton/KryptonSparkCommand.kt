package me.lucko.spark.krypton

import me.lucko.spark.common.SparkPlatform
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.command.SimpleCommand

class KryptonSparkCommand(private val platform: SparkPlatform) : SimpleCommand {

    override fun execute(sender: Sender, args: Array<String>) {
        platform.executeCommand(KryptonCommandSender(sender), args)
    }

    override fun suggest(sender: Sender, args: Array<String>): List<String> {
        return platform.tabCompleteCommand(KryptonCommandSender(sender), args)
    }
}