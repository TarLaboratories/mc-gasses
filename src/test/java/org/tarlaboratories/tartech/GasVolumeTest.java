package org.tarlaboratories.tartech;

import org.junit.jupiter.api.Test;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.gas.GasVolume;

public class GasVolumeTest {
    @Test
    void testGasVolumeCodec() {
        GasVolume sampleValue = (new GasVolume()).addGas(Chemical.SULFUR_DIOXIDE, 228).addGas(Chemical.OXYGEN, 12).addVolume(123).setRadioactivity(5).setTemperature(-273);
        CodecTest.testCodec(sampleValue, GasVolume.CODEC);
    }
}
