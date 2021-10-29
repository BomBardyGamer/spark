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

import me.lucko.spark.common.command.sender.AbstractCommandSender
import net.kyori.adventure.text.Component
import org.kryptonmc.api.adventure.toLegacySectionText
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.entity.player.Player

class KryptonCommandSender(sender: Sender) : AbstractCommandSender<Sender>(sender) {

    override fun getName() = delegate.name.toLegacySectionText()

    override fun getUniqueId() = (delegate as? Player)?.uuid

    override fun sendMessage(message: Component) {
        delegate.sendMessage(message)
    }

    override fun hasPermission(permission: String) = delegate.hasPermission(permission)
}
