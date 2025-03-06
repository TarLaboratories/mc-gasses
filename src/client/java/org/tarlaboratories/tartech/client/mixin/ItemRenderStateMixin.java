package org.tarlaboratories.tartech.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tarlaboratories.tartech.items.ModItems;
import org.tarlaboratories.tartech.client.ModifiableItemStack;
import org.tarlaboratories.tartech.client.TartechClient;

import java.util.List;

@Mixin(ItemRenderState.class)
public abstract class ItemRenderStateMixin implements ModifiableItemStack {
    @Shadow public abstract Transformation getTransformation();

    @Shadow boolean leftHand;
    @Unique public ItemStack stack;

    @Inject(at = @At("TAIL"), method="render")
    public void renderText(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo info) {
        if (!stack.isOf(ModItems.GAS_ANALYSER_ITEM)) return;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (TartechClient.player_volume == null) return;
        assert MinecraftClient.getInstance().player != null;
        matrices.push();
        getTransformation().apply(leftHand, matrices);
        matrices.translate(-0.5, -0.5, -0.50001);
        matrices.scale(1/16f, 1/16f, 1/16f);
        matrices.scale(1/27f, 1/27f, 1/27f);
        List<Text> text = TartechClient.player_volume.getInfo(MinecraftClient.getInstance().player.isInCreativeMode());
        for (int i = 0; i < text.size(); i++) {
            MutableText line = text.get(i).copy();
            float x = 0, width = textRenderer.getWidth(line);
            if (width > 27*6) x = (Util.getMeasuringTimeMs()*27/1000f) % (width - 27*6);
            while (x > 0) {
                x -= textRenderer.getWidth(line.asTruncatedString(1));
                line = Text.literal(line.getString().substring(1)).setStyle(line.getStyle());
            }
            line = Text.literal(textRenderer.trimToWidth(line, 27*6).getString()).setStyle(line.getStyle());
            textRenderer.draw(
                    line,
                    -x,
                    8 * i,
                    0x00FF00,
                    false,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0x000000, 255
            );
        }
        matrices.pop();
    }

    @Unique
    public void setItemStack(ItemStack itemStack) {
        stack = itemStack;
    }
}
