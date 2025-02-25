package org.tarlaboratories.tartech.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.tarlaboratories.tartech.ModBlockEntities;
import org.tarlaboratories.tartech.ModFluids;
import org.tarlaboratories.tartech.blockentities.PipeBlockEntity;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.client.blockentityrenderers.PipeBlockEntityRenderer;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;
import org.tarlaboratories.tartech.gas.GasVolume;
import org.tarlaboratories.tartech.networking.*;

import java.util.Objects;

public class TartechClient implements ClientModInitializer {
    private static GasVolume player_volume = null;
    private static long last_volume_update = Util.getMeasuringTimeMs();

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
        ClientPlayNetworking.registerGlobalReceiver(GasVolumeDataPayload.ID, ((payload, context) -> {
            player_volume = payload.volume();
            last_volume_update = Util.getMeasuringTimeMs();
        }));
    }

    public void registerHUDRenderers() {
        HudRenderCallback.EVENT.register((context, ticks) -> {
            if (!RenderingUtils.shouldRenderDebug()) return;
            if (player_volume == null || last_volume_update + 500 < Util.getMeasuringTimeMs()) {
                if (MinecraftClient.getInstance().player == null) return;
                ClientPlayNetworking.send(new GasVolumeDataRequestPayload(MinecraftClient.getInstance().player.getBlockPos()));
                if (player_volume == null) return;
            }
            Text text = player_volume.getInfo(true);
            String[] lines = text.getString().split("\n");
            for (int i = 0; i < lines.length; i++)
                context.drawText(MinecraftClient.getInstance().textRenderer, lines[i], 0, 8*i, 0xFFFFFF, true);
        });
    }

    public void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(ModBlockEntities.PIPE, PipeBlockEntityRenderer::new);
    }

    @Override
    public void onInitializeClient() {
        registerFluidRenderHandlers();
        registerNetworkingReceivers();
        registerBlockEntityRenderers();
        registerHUDRenderers();
    }
}
