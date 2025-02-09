package org.tarlaboratories.tartech.chemistry;

import org.junit.jupiter.api.Test;
import org.tarlaboratories.tartech.CodecTest;

public class ChemicalElementTest {
    @Test
    void testChemicalElementCodec() {
        CodecTest.testCodec(new ChemicalElement("He"), ChemicalElement.CODEC);
    }
}
