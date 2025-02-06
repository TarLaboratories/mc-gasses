package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tarlaboratories.tartech.chemistry.Chemical;

public class ModEventListeners {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void initialize() {
        UseItemCallback.EVENT.register(((player, _world, hand) -> {
            if (_world instanceof ServerWorld world) {
                StateSaverAndLoader state = StateSaverAndLoader.getWorldState(world);
                if (player.getStackInHand(hand).isOf(ModItems.TEST_ITEM)) {
                    state.reinitializeDataAtPos(player.getBlockPos());
                } else if (player.getStackInHand(hand).isOf(Items.STICK)) state.updateVolumesInChunk(player.getBlockPos());
                else if(player.getStackInHand(hand).isOf(ModItems.GAS_TEST_ITEM)) state.reinitializeDataAtPos(player.getBlockPos());
                else if (player.getStackInHand(hand).isOf(Items.BOWL)) state.addGasAtPos(player.getBlockPos(), Chemical.WATER, 10);
            }
            return ActionResult.PASS;
        }));
    }
}
