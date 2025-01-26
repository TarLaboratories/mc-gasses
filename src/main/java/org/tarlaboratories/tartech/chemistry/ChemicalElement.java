package org.tarlaboratories.tartech.chemistry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ChemicalElement {
    public static final ChemicalElement HYDROGEN = new ChemicalElement("H");
    public static final ChemicalElement OXYGEN = new ChemicalElement("O");
    public static final ChemicalElement NITROGEN = new ChemicalElement("N");
    public static final ChemicalElement CARBON = new ChemicalElement("C");

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
}
