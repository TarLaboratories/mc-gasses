package org.tarlaboratories.tartech.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.tarlaboratories.tartech.ModBlockEntities;
import org.tarlaboratories.tartech.ModFluids;
import org.tarlaboratories.tartech.blockentities.PipeBlockEntity;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.client.blockentityrenderers.PipeBlockEntityRenderer;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;
import org.tarlaboratories.tartech.networking.ChemicalNetworkDataPayload;
import org.tarlaboratories.tartech.networking.ChemicalNetworkIdChangePayload;
import org.tarlaboratories.tartech.networking.GasCondensationPayload;
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
            BlockPos pos = payload.pos();
            Objects.requireNonNull(context.client().world).setBlockState(pos, Blocks.AIR.getDefaultState());
            context.client().particleManager.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0.1, 0);
        });
        ClientPlayNetworking.registerGlobalReceiver(GasCondensationPayload.ID, (payload, context) -> {
            BlockPos pos = payload.pos();
            Chemical gas = Chemical.fromString(payload.gas());
            Objects.requireNonNull(context.client().world).setBlockState(pos, ModFluids.CHEMICAL_FLUIDS.get(gas).getLeft().getDefaultState().getBlockState());
        });
        ClientPlayNetworking.registerGlobalReceiver(ChemicalNetworkIdChangePayload.ID, ((payload, context) -> {
            BlockPos pos = payload.pos();
            int new_id = payload.new_id();
            if (Objects.requireNonNull(context.client().world).getBlockEntity(pos) instanceof PipeBlockEntity blockEntity) blockEntity.setChemicalNetworkId(new_id);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ChemicalNetworkDataPayload.ID, ChemicalNetworkData::receivePayload);
    }

    public void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(ModBlockEntities.PIPE_BLOCK_ENTITY, PipeBlockEntityRenderer::new);
    }

    @Override
    public void onInitializeClient() {
        registerFluidRenderHandlers();
        registerNetworkingReceivers();
        registerBlockEntityRenderers();
    }
}
