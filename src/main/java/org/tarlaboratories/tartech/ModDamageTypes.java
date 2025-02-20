package org.tarlaboratories.tartech;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> NO_AIR = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Tartech.MOD_ID, "no_air"));
    public static RegistryEntry<DamageType> noAir(@NotNull ServerWorld world) {
        return world.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getEntry(NO_AIR.getValue()).orElseThrow();
    }
}
