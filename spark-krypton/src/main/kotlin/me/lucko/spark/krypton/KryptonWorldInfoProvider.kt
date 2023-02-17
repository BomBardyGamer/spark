package me.lucko.spark.krypton

import me.lucko.spark.common.platform.world.ChunkInfo
import me.lucko.spark.common.platform.world.WorldInfoProvider
import org.kryptonmc.api.Server

class KryptonWorldInfoProvider(private val server: Server) : WorldInfoProvider {

    override fun pollCounts(): WorldInfoProvider.CountsResult {
        val players = server.players.size
        var entities = 0
        var chunks = 0

        server.worldManager.worlds.forEach { (_, world) ->
            entities += world.entities.size
            chunks += world.chunks.size
        }

        return WorldInfoProvider.CountsResult(players, entities, 0, chunks)
    }

    // TODO: When Krypton supports getting entities in a chunk, properly implement this
    override fun pollChunks(): WorldInfoProvider.ChunksResult<out ChunkInfo<*>> = WorldInfoProvider.ChunksResult()
}