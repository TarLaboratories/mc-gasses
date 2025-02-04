package org.tarlaboratories.tartech;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModEntities {
    private static final Logger LOGGER = LogManager.getLogger();

    private static <T extends Entity> EntityType<T> register(RegistryKey<EntityType<?>> key, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, key, entityType);
    }


    public static void initialize() {}
}
