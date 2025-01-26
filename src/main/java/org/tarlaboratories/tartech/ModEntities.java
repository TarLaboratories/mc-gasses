package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tarlaboratories.tartech.entities.DataEntity;

import java.util.stream.Stream;

public class ModEntities {
    private static final Logger LOGGER = LogManager.getLogger();

    private static <T extends Entity> EntityType<T> register(RegistryKey<EntityType<?>> key, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, key, entityType);
    }

    public static RegistryKey<EntityType<?>> DATA_ENTITY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Tartech.MOD_ID, "data_entity"));
    public static EntityType<DataEntity> DATA_ENTITY = register(DATA_ENTITY_KEY, EntityType.Builder.create(DataEntity::new, SpawnGroup.MISC)
//            .disableSummon()
            .dropsNothing()
            .dimensions(1, 1)
            .makeFireImmune()
            .build(DATA_ENTITY_KEY)
    );


    public static void initialize() {
        FabricDefaultAttributeRegistry.register(DATA_ENTITY, DataEntity.createMobAttributes().add(EntityAttributes.GRAVITY, 0));
        ServerChunkEvents.CHUNK_GENERATE.register((world, chunk) -> {
            Entity entity = (new DataEntity(DATA_ENTITY, world)).teleportTo(new TeleportTarget(world, chunk.getPos().getCenterAtY(-100).toCenterPos(), Vec3d.ZERO, 0, 0, TeleportTarget.NO_OP));
            assert entity != null;
            world.addEntities(Stream.of(entity));
        });
    }
}
