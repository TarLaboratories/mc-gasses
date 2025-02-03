package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tarlaboratories.tartech.entities.DataEntity;

public class ModEntities {
    private static final Logger LOGGER = LogManager.getLogger();

    private static <T extends Entity> EntityType<T> register(RegistryKey<EntityType<?>> key, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, key, entityType);
    }

    public static RegistryKey<EntityType<?>> DATA_ENTITY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Tartech.MOD_ID, "data_entity"));
    public static EntityType<DataEntity> DATA_ENTITY = register(DATA_ENTITY_KEY, EntityType.Builder.create(DataEntity::new, SpawnGroup.MISC)
            .disableSummon()
            .dropsNothing()
            .dimensions(1, 1)
            .makeFireImmune()
            .spawnableFarFromPlayer()
            .build(DATA_ENTITY_KEY)
    );


    public static void initialize() {
        FabricDefaultAttributeRegistry.register(DATA_ENTITY, DataEntity.createMobAttributes().add(EntityAttributes.GRAVITY, 0));
        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
            DataEntity.updateVolumeAtPos(blockHitResult.getBlockPos(), world);
            return ActionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            DataEntity.updateVolumeAtPos(pos, world);
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (DataEntity.initializeVolumesInChunk(player.getChunkPos(), world) == null) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }
}
