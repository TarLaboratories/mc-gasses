package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModEventListeners {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void initialize() {
        UseItemCallback.EVENT.register(((player, _world, hand) -> {
            if (_world instanceof ServerWorld world) {
                StateSaverAndLoader state = StateSaverAndLoader.getWorldState(world);
                if (player.getStackInHand(hand).isOf(ModItems.TEST_ITEM)) {
                    state.reinitializeDataAtPos(player.getBlockPos());
                } else if (player.getStackInHand(hand).isOf(ModItems.GAS_TEST_ITEM)) state.updateVolumesInChunk(player.getBlockPos());
            }
            return ActionResult.PASS;
        }));
    }
}
