package org.tarlaboratories.tartech.chemistry;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ChemicalElement {
    public static final Codec<ChemicalElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("element").forGetter(ChemicalElement::getElement)
    ).apply(instance, ChemicalElement::new));

    public String element;

    public ChemicalElement(String s) {
        this.element = s;
    }

    public String getElement() {
        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChemicalElement that = (ChemicalElement) o;
        return Objects.equal(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }
}
