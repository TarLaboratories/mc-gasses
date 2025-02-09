package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class GameTests implements FabricGameTest {
    public static final Logger LOGGER = LogManager.getLogger();

    public static <T> void testCodec(@NotNull TestContext ctx, @NotNull T sampleValue, final @NotNull Codec<T> CODEC) {
        LOGGER.info(sampleValue.toString());
        DataResult<NbtElement> encodingResult = CODEC.encodeStart(NbtOps.INSTANCE, sampleValue);
        ctx.assertTrue(encodingResult.isSuccess(), "Encoding failed: " + encodingResult.error());
        NbtElement encodedValue = encodingResult.result().orElse(null);
        ctx.assertFalse(encodedValue == null, "encodedValue is null");
        DataResult<T> decodingResult = CODEC.parse(NbtOps.INSTANCE, encodedValue);
        ctx.assertTrue(decodingResult.isSuccess(), "Decoding failed: " + decodingResult.error());
        T decodedValue = decodingResult.result().orElse(null);
        ctx.assertFalse(decodedValue == null, "decodedValue is null");
        ctx.assertEquals(sampleValue, decodedValue, "Encoded and decoded object is not equal to initial object");
    }

    @GameTest(batchId = "codecTestsBatch")
    public void testGasDataCodec(@NotNull TestContext ctx) {
        World world = ctx.getWorld();
        GasData gasData = GasData.initializeVolumesInChunk(world.getChunk(BlockPos.ofFloored(ctx.getTestBox().getCenter())), world);
        testCodec(ctx, gasData, GasData.CODEC);
        ctx.complete();
    }
}
