package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tarlaboratories.tartech.entities.GasData;

public class ModEntities {
    private static final Logger LOGGER = LogManager.getLogger();

    private static <T extends Entity> EntityType<T> register(RegistryKey<EntityType<?>> key, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, key, entityType);
    }


    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
         //   DataEntity.updateVolumeAtPos(blockHitResult.getBlockPos(), world);
            return ActionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
           //     DataEntity.updateVolumeAtPos(pos, world);
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (GasData.initializeVolumesInChunk(player.getChunkPos(), world) == null) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }
}
