package org.tarlaboratories.tartech.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.tarlaboratories.tartech.ModItems;

public class RenderingUtils {
    private static BlockPos previous_highlight = null;
    private static BlockPos highlight_destination = null;
    private static long last_highlight_change = Util.getMeasuringTimeMs();

    public static void highlight(BlockPos pos, MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemRenderer itemRenderer) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            previous_highlight = null;
            highlight_destination = null;
            last_highlight_change = Util.getMeasuringTimeMs();
            return;
        }
        float delta = (Util.getMeasuringTimeMs() - last_highlight_change)/250f;
        if ((previous_highlight == null && highlight_destination == null) || Float.compare(delta, 2f) >= 0) {
            previous_highlight = highlight_destination = pos;
            last_highlight_change = Util.getMeasuringTimeMs();
            delta = 0;
        } else if (Float.compare(delta, 1) >= 0 || previous_highlight == null || highlight_destination == null || previous_highlight == highlight_destination) {
            previous_highlight = highlight_destination;
            highlight_destination = pos;
            last_highlight_change = Util.getMeasuringTimeMs();
            delta = 0;
        }
        Vec3d tmp = highlight_destination.toCenterPos().subtract(previous_highlight.toCenterPos());
        MatrixStack idk = new MatrixStack();
        idk.push();
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.translate(highlight_destination.toCenterPos().subtract(pos.toCenterPos()));
        matrices.translate(tmp.negate());
        matrices.translate(tmp.multiply(delta));
        itemRenderer.renderItem(ModItems.RENDERING_ITEM.getWithModel("outline"), ModelTransformationMode.GROUND, 255, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean shouldRenderDebug() {
        if (MinecraftClient.getInstance().player == null) return false;
        return MinecraftClient.getInstance().player.getOffHandStack().isIn(ModItems.DEBUG_TAG);
    }
}
