package org.tarlaboratories.tartech.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface BlockModificationCallback {
    Event<BlockModificationCallback> EVENT = EventFactory.createArrayBacked(BlockModificationCallback.class,
            (listeners) -> (world, pos) -> {
                for (BlockModificationCallback listener : listeners) {
                    listener.call(world, pos);
                }
            });

    void call(ServerWorld world, BlockPos pos);
}
