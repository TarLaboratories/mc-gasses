package org.tarlaboratories.tartech.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.ModFluids;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;
import org.tarlaboratories.tartech.networking.LiquidEvaporationPayload;

import java.util.Objects;

public class TartechClient implements ClientModInitializer {
    public void registerFluidRenderHandlers() {
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

    public void registerNetworkingReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(LiquidEvaporationPayload.ID, (payload, context) -> {
            Objects.requireNonNull(context.client().world).setBlockState(payload.pos(), Blocks.AIR.getDefaultState());
        });
    }

    @Override
    public void onInitializeClient() {
        registerFluidRenderHandlers();
        registerNetworkingReceivers();
    }
}
