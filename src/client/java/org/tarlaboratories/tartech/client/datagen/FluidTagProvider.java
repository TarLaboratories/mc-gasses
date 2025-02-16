package org.tarlaboratories.tartech.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import org.tarlaboratories.tartech.ModFluids;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.concurrent.CompletableFuture;

public class FluidTagProvider extends FabricTagProvider<Fluid> {
    public FluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.FLUID, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        FabricTagBuilder builder = getOrCreateTagBuilder(ModFluids.CHEMICAL_FLUID_TAG);
        for (Chemical chemical : ModFluids.CHEMICAL_FLUIDS.keySet()) {
            builder.add(ModFluids.CHEMICAL_FLUIDS.get(chemical).getLeft());
            builder.add(ModFluids.CHEMICAL_FLUIDS.get(chemical).getRight());
        }
    }
}
