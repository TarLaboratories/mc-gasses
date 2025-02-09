package org.tarlaboratories.tartech.chemistry;

import org.junit.jupiter.api.Test;
import org.tarlaboratories.tartech.CodecTest;

public class ChemicalTest {
    @Test
    void testChemicalCodec() {
        CodecTest.testCodec(Chemical.fromString("(H2)(SO4)228(LO3H5A)"), Chemical.CODEC);
    }
}
