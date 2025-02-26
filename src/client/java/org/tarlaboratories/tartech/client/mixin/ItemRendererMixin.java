package org.tarlaboratories.tartech.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tarlaboratories.tartech.ModItems;
import org.tarlaboratories.tartech.client.TartechClient;

import java.util.List;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(at = @At("TAIL"),method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V")
    public void renderGasVolumeInfo(LivingEntity entity, @NotNull ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (!stack.isOf(ModItems.GAS_ANALYSER_ITEM)) return;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (TartechClient.player_volume == null) return;
        assert MinecraftClient.getInstance().player != null;
        matrices.push();
        matrices.translate(0, 0.2, 0.1);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15));
        matrices.translate(-0.09, -0.149, 0.04);
        matrices.scale(1/32f, 1/32f, 1/32f);
        matrices.scale(1/18f, 1/18f, 1/18f);
        List<Text> text = TartechClient.player_volume.getInfo(MinecraftClient.getInstance().player.isInCreativeMode());
        for (int i = 0; i < text.size(); i++)
            textRenderer.draw(
                    text.get(i),
                    0,
                    8*i,
                    0x00FF00,
                    false,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0x000000, 255
            );
        matrices.pop();
    }
}
