package org.tarlaboratories.tartech.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;

public interface ChunkModificationCallback {
    Event<ChunkModificationCallback> EVENT = EventFactory.createArrayBacked(ChunkModificationCallback.class,
            (listeners) -> (world, chunk) -> {
                for (ChunkModificationCallback listener : listeners) {
                    listener.call(world, chunk);
                }
            });

    void call(ServerWorld world, Chunk chunk);
}
