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

import me.lucko.spark.common.tick.AbstractTickHook
import org.kryptonmc.api.event.EventListener
import org.kryptonmc.api.event.EventNode
import org.kryptonmc.api.event.server.TickEvent
import org.kryptonmc.api.event.server.TickStartEvent

class KryptonTickHook(private val eventNode: EventNode<TickEvent>) : AbstractTickHook() {

    private val listener = EventListener.of(TickStartEvent::class.java) { onTick() }

    override fun start() {
        eventNode.registerListener(listener)
    }

    override fun close() {
        eventNode.unregisterListener(listener)
    }
}
