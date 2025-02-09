package org.tarlaboratories.tartech.chemistry;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ChemicalPart {
    public static final ChemicalPart HYDROGEN = primitiveOf("H");
    public static final ChemicalPart OXYGEN = primitiveOf("O");
    public static final ChemicalPart WATER = primitiveOf("H").addElement(ChemicalElement.OXYGEN, 1);
    public static final ChemicalPart SULFUR_DIOXIDE = primitiveOf("O").addElement(ChemicalElement.OXYGEN, 1);

    public static final Codec<ChemicalPart> CODEC = Codec.STRING.xmap(ChemicalPart::fromString, ChemicalPart::toString);

    public static ChemicalPart primitiveOf(String element) {
        return (new ChemicalPart()).addElement(new ChemicalElement(element), 2);
    }

    public Map<ChemicalElement, Integer> contents;

    public ChemicalPart() {
        this.contents = new HashMap<>();
    }

    public ChemicalPart(Map<ChemicalElement, Integer> contents) {
        this.contents = new HashMap<>(contents);
    }

    public static @NotNull ChemicalPart fromString(@NotNull String s) throws InvalidChemicalStringException {
        ChemicalPart out = new ChemicalPart();
        int elementStart = -1, elementEnd = -1;
        StringBuilder num = new StringBuilder();
        try {
            for (int i = 0; i < s.length(); i++) {
                if (!Character.isLetterOrDigit(s.charAt(i))) throw new InvalidChemicalStringException(String.format("Unexpected character '%s' at position %d", s.charAt(i), i));
                if (Character.isUpperCase(s.charAt(i))) {
                    if (elementStart != -1) {
                        if (elementEnd == -1) elementEnd = i;
                        if (!num.toString().isBlank()) out.addElement(new ChemicalElement(s.substring(elementStart, elementEnd)), Integer.parseInt(num.toString()));
                        else out.addElement(new ChemicalElement(s.substring(elementStart, elementEnd)), 1);
                        elementStart = i;
                        elementEnd = -1;
                    } else {
                        if (i != 0) throw new InvalidChemicalStringException("Unexpected characters at start of string");
                        elementStart = 0;
                    }
                    num = new StringBuilder();
                }
                if (Character.isDigit(s.charAt(i))) {
                    num.append(s.charAt(i));
                    if (elementEnd == -1) elementEnd = i;
                }
            }
            if (elementStart != -1) {
                if (elementEnd == -1) elementEnd = s.length();
                if (!num.toString().isBlank()) out.addElement(new ChemicalElement(s.substring(elementStart, elementEnd)), Integer.parseInt(num.toString()));
                else out.addElement(new ChemicalElement(s.substring(elementStart, elementEnd)), 1);
            }
        } catch (NumberFormatException ex) {
            throw new InvalidChemicalStringException(ex.getMessage());
        }
        return out;
    }

    public ChemicalPart addElement(ChemicalElement element, int amount) {
        this.contents.put(element, this.contents.getOrDefault(element, 0) + amount);
        return this;
    }

    public Map<ChemicalElement, Integer> getContents() {
        return Map.copyOf(contents);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (ChemicalElement e : this.contents.keySet()) {
            s.append(e.getElement());
            if (this.contents.get(e) > 1) s.append(this.contents.get(e).toString());
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChemicalPart that = (ChemicalPart) o;
        return Objects.equal(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(contents);
    }
}
