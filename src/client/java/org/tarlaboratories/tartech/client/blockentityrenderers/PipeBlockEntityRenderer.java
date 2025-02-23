package org.tarlaboratories.tartech.client.blockentityrenderers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.tarlaboratories.tartech.ChemicalNetwork;
import org.tarlaboratories.tartech.ModItems;
import org.tarlaboratories.tartech.blockentities.PipeBlockEntity;
import org.tarlaboratories.tartech.client.ChemicalNetworkData;

import java.util.Objects;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity> {
    private final BlockEntityRendererFactory.Context context;

    public PipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(PipeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.getOffHandStack().isIn(ModItems.DEBUG_TAG)) return;
        matrices.push();
        matrices.translate(0.5, 1, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.scale(1/18f, 1/18f, 1/18f);
        String text = Integer.toString(entity.getChemicalNetworkId());
        int width = context.getTextRenderer().getWidth(text);
        context.getTextRenderer().draw(
                text,
                -width/2f, -4f,
                0xFFFFFF,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0, light
        );
        Vec3d idk = player.raycast(player.getBlockInteractionRange(), tickDelta, false).getPos();
        if (!Objects.equals(entity.getPos(), BlockPos.ofFloored(idk))) {
            matrices.pop();
            return;
        }
        matrices.translate(-9, -9, -18);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.scale(1/4f, 1/4f, 1/4f);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(180 - Math.round(player.getYaw()/90)*90), 36, 0,-36);
        ChemicalNetwork data = ChemicalNetworkData.getOrRequest(entity.getChemicalNetworkId());
        Text chemical_network_info;
        if (data == null) chemical_network_info = Text.of("Requesting data...");
        else chemical_network_info = data.getInfo();
        String[] lines = chemical_network_info.getString().split("\n");
        for (int i = 0; i < lines.length; i++)
            context.getTextRenderer().draw(
                    lines[i],
                    0, 8*i,
                    0xFF00FF,
                    true,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0, light
            );
        matrices.pop();
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        context.getItemRenderer().renderItem(ModItems.RENDERING_ITEM.getWithModel("outline"), ModelTransformationMode.GROUND, 255, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
        matrices.pop();
    }
}
