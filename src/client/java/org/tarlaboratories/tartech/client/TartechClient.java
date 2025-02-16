package org.tarlaboratories.tartech.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.ModFluids;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;

public class TartechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Chemical.forEachChemical(((chemical, properties) -> {
            ChemicalFluid.Still still = ModFluids.CHEMICAL_FLUIDS.get(chemical).getLeft();
            ChemicalFluid.Flowing flowing = ModFluids.CHEMICAL_FLUIDS.get(chemical).getRight();
            if (properties.canBeSolid() && properties.crystallizationTemperature() >= 200) {
                FluidRenderHandlerRegistry.INSTANCE.register(still, flowing, new SimpleFluidRenderHandler(
                        Identifier.ofVanilla("block/lava_still"),
                        Identifier.ofVanilla("block/lava_flow"),
                        chemical.getProperties().color()
                ));
                BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), still, flowing);
            } else {
                FluidRenderHandlerRegistry.INSTANCE.register(still, flowing, new SimpleFluidRenderHandler(
                        Identifier.ofVanilla("block/water_still"),
                        Identifier.ofVanilla("block/water_flow"),
                        chemical.getProperties().color()
                ));
                BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), still, flowing);
            }
            return null;
        }));
    }
}
