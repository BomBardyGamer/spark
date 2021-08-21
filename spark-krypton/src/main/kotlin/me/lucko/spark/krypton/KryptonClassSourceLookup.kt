package me.lucko.spark.krypton

import me.lucko.spark.common.util.ClassSourceLookup
import org.kryptonmc.api.plugin.PluginManager

class KryptonClassSourceLookup(private val pluginManager: PluginManager) : ClassSourceLookup.ByClassLoader() {

    override fun identify(loader: ClassLoader): String? {
        if (PLUGIN_CLASS_LOADER.isInstance(loader)) pluginManager.plugins.forEach {
            val instance = it.instance
            if (instance != null && instance.javaClass.classLoader === loader) return it.description.name.ifEmpty { it.description.id }
        }
        return null
    }

    companion object {

        private val PLUGIN_CLASS_LOADER = Class.forName("org.kryptonmc.krypton.plugin.PluginClassLoader")
    }
}
