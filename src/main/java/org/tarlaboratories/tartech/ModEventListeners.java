package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public class ModEventListeners {
    public static void initialize() {
        UseItemCallback.EVENT.register(((player, _world, hand) -> {
            if (_world instanceof ServerWorld world && !player.getActiveItem().isEmpty()) {
                StateSaverAndLoader state = StateSaverAndLoader.getWorldState(world);
                state.addGasDataForChunk(world, world.getChunk(player.getBlockPos()));
            }
            return ActionResult.PASS;
        }));
    }
}
