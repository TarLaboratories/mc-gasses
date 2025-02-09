package org.tarlaboratories.tartech.chemistry;

import org.junit.jupiter.api.Test;
import org.tarlaboratories.tartech.CodecTest;

public class ChemicalPartTest {
    @Test
    void testChemicalPartCodec() {
        CodecTest.testCodec(ChemicalPart.fromString("H2SO4He"), ChemicalPart.CODEC);
    }
}
