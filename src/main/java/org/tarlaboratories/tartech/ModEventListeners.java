package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tarlaboratories.tartech.gas.GasData;

public class ModEventListeners {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();

    public static void initialize() {
        UseItemCallback.EVENT.register(((player, _world, hand) -> {
            if (_world instanceof ServerWorld world) {
                StateSaverAndLoader state = StateSaverAndLoader.getWorldState(world);
                if (player.getStackInHand(hand).isOf(ModItems.TEST_ITEM)) {
                    state.reinitializeDataAtPos(player.getBlockPos());
                } else if (player.getStackInHand(hand).isOf(ModItems.GAS_TEST_ITEM) || player.getStackInHand(hand).isIn(ModItems.CHEMICAL_FLUID_BUCKET_TAG)) state.updateVolumesInChunk(player.getBlockPos());
            }
            return ActionResult.PASS;
        }));
        ServerTickEvents.START_WORLD_TICK.register((world) -> {
            for (ServerPlayerEntity player : world.getPlayers((player) -> !player.isCreative() && !player.isSpectator())) {
                PlayerData data = StateSaverAndLoader.getPlayerData(player);
                if (!GasData.playerBreathe(player)) {
                    data.air--;
                    if (data.air <= -20) {
                        player.damage(world, new DamageSource(ModDamageTypes.noAir(world)), 4);
                        data.air = 0;
                    }
                } else if (data.air < 300) data.air++;
            }
        });
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            for (ServerPlayerEntity player : world.getPlayers((player) -> !player.isCreative() && !player.isSpectator())) {
                player.setAir(StateSaverAndLoader.getPlayerData(player).getAir());
            }
        });
    }
}
