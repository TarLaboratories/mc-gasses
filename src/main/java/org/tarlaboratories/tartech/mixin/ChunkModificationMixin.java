package org.tarlaboratories.tartech.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tarlaboratories.tartech.events.ChunkModificationCallback;

@Mixin(World.class)
public class ChunkModificationMixin {
    @Unique
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"), method="setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z")
    private void onChunkModified(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> info) {
        World world = (World) (Object) this;
        if (world instanceof ServerWorld serverWorld) {
            ChunkModificationCallback.EVENT.invoker().call(serverWorld, serverWorld.getChunk(pos));
        }
    }
}
