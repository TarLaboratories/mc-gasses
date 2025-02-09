package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

public class CodecTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public static <T> void testCodec(@NotNull T sampleValue, final @NotNull Codec<T> CODEC) {
        LOGGER.info(sampleValue.toString());
        DataResult<NbtElement> encodingResult = CODEC.encodeStart(NbtOps.INSTANCE, sampleValue);
        Assertions.assertTrue(encodingResult.isSuccess(), () -> "Encoding failed: " + encodingResult.error().orElseThrow().message());
        NbtElement encodedValue = encodingResult.result().orElse(null);
        Assertions.assertNotNull(encodedValue, "encodedValue is null");
        DataResult<T> decodingResult = CODEC.parse(NbtOps.INSTANCE, encodedValue);
        Assertions.assertTrue(decodingResult.isSuccess(), () -> "Decoding failed: " + decodingResult.error().orElseThrow().message());
        T decodedValue = decodingResult.result().orElse(null);
        Assertions.assertNotNull(decodedValue, "decodedValue is null");
        Assertions.assertEquals(sampleValue, decodedValue, "Encoded and decoded object is not equal to initial object");
    }
}
