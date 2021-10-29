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

import me.lucko.spark.common.util.ClassSourceLookup
import org.kryptonmc.api.plugin.PluginManager

class KryptonClassSourceLookup(private val pluginManager: PluginManager) : ClassSourceLookup.ByClassLoader() {

    override fun identify(loader: ClassLoader): String? {
        if (PLUGIN_CLASS_LOADER == null || !PLUGIN_CLASS_LOADER.isInstance(loader)) return null
        pluginManager.plugins.forEach {
            val instance = it.instance
            if (instance != null && instance.javaClass.classLoader === loader) {
                return it.description.name.ifEmpty { it.description.id }
            }
        }
        return null
    }

    companion object {

        // We may be working with an implementation of the Krypton API that isn't Krypton.
        private val PLUGIN_CLASS_LOADER = try {
            Class.forName("org.kryptonmc.krypton.plugin.PluginClassLoader")
        } catch (exception: Exception) {
            null
        }
    }
}
