package org.tarlaboratories.tartech;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class ModComponents {
    public static void initialize() {}

    public static final ComponentType<UUID> DATA_STORAGE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Tartech.MOD_ID, "data_storage_component"),
            ComponentType.<UUID>builder().codec(Uuids.CODEC).build()
    );
}
