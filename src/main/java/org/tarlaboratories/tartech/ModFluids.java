package org.tarlaboratories.tartech;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;

import java.util.Map;

public class ModFluids {
    private static <T extends Fluid> T register(String name, T fluid) {
        return Registry.register(Registries.FLUID, Identifier.of(Tartech.MOD_ID, name), fluid);
    }

    public static final Map<Chemical, Pair<ChemicalFluid.Still, ChemicalFluid.Flowing>> CHEMICAL_FLUIDS = Chemical.forEachChemical((chemical, properties) -> new Pair<>(register("chemical_fluid_" + chemical.toIdentifierString(), new ChemicalFluid.Still(chemical)), register("flowing_chemical_fluid_" + chemical.toIdentifierString(), new ChemicalFluid.Flowing(chemical))));

    public static void initialize() {}
}
