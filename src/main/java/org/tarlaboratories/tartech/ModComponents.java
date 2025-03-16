package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
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

    public static final ComponentType<String> STORED_FLUID = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Tartech.MOD_ID, "stored_fluid"),
            ComponentType.<String>builder().codec(Codec.STRING).build()
    );

    public static final ComponentType<Double> STORED_FLUID_AMOUNT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Tartech.MOD_ID, "stored_fluid_amount"),
            ComponentType.<Double>builder().codec(Codec.DOUBLE).build()
    );
}
