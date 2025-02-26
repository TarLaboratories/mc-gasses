package org.tarlaboratories.tartech.client.blockentityrenderers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.tarlaboratories.tartech.ChemicalNetwork;
import org.tarlaboratories.tartech.blockentities.PipeBlockEntity;
import org.tarlaboratories.tartech.client.ChemicalNetworkData;
import org.tarlaboratories.tartech.client.RenderingUtils;

import java.util.List;
import java.util.Objects;

public class PipeBlockEntityRenderer implements BlockEntityRenderer<PipeBlockEntity> {
    private final BlockEntityRendererFactory.Context context;

    public PipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(PipeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (!RenderingUtils.shouldRenderDebug()) return;
        assert player != null;
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
        if (MinecraftClient.getInstance().crosshairTarget == null) return;
        Vec3d idk = MinecraftClient.getInstance().crosshairTarget.getPos();
        if (!Objects.equals(entity.getPos(), BlockPos.ofFloored(idk))) {
            matrices.pop();
            return;
        }
        matrices.translate(-9, -9, -18);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.scale(1/4f, 1/4f, 1/4f);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(180 - Math.round(player.getYaw()/90)*90), 36, 0,-36);
        ChemicalNetwork data = ChemicalNetworkData.getOrRequest(entity.getChemicalNetworkId());
        List<Text> chemical_network_info;
        if (data == null) chemical_network_info = List.of(Text.of("Requesting data..."));
        else chemical_network_info = data.getInfo();
        for (int i = 0; i < chemical_network_info.size(); i++)
            context.getTextRenderer().draw(
                    chemical_network_info.get(i),
                    0, 8*i,
                    0xFF00FF,
                    true,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0, light
            );
        matrices.pop();
        RenderingUtils.highlight(entity.getPos(), matrices, vertexConsumers, context.getItemRenderer());
    }
}
