package org.tarlaboratories.tartech.chemistry;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Chemical {
    public static final Chemical HYDROGEN = primitiveOf("H");
    public static final Chemical OXYGEN = primitiveOf("O");
    public static final Chemical WATER = new Chemical(ChemicalPart.WATER);
    public static final Chemical SULFUR_DIOXIDE = new Chemical(ChemicalPart.SULFUR_DIOXIDE);

    public static final Codec<Chemical> CODEC = Codec.STRING.xmap(Chemical::fromString, Chemical::toString);

    @Contract("_ -> new")
    public static @NotNull Chemical primitiveOf(String element) {
        return new Chemical(ChemicalPart.primitiveOf(element));
    }
    public Map<ChemicalPart, Integer> contents;

    public Chemical() {
        contents = new HashMap<>();
    }

    public Chemical(ChemicalPart @NotNull ... a) {
        contents = new HashMap<>();
        for (ChemicalPart i : a) contents.put(i, 1);
    }

    public Chemical(Map<ChemicalPart, Integer> a) {
        contents = new HashMap<>(a);
    }

    public static @NotNull Chemical fromString(@NotNull String s) throws InvalidChemicalStringException {
        if (s.isEmpty()) throw new InvalidChemicalStringException("Expected non-empty string");
        if (s.charAt(0) != '(') {
            return new Chemical(ChemicalPart.fromString(s));
        }
        int chemicalPartStart = -1;
        Chemical out = new Chemical();
        ChemicalPart tmp = null;
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '(':
                    if (chemicalPartStart != -1) throw new InvalidChemicalStringException(String.format("Unexpected '(' in string at position %d", i));
                    chemicalPartStart = i + 1;
                    if (tmp != null) {
                        if (!Objects.equals(num.toString(), "")) out.add(tmp, Integer.parseInt(num.toString()));
                        else out.add(tmp);
                        tmp = null;
                    }
                    num = new StringBuilder();
                    break;
                case ')':
                    if (chemicalPartStart == -1) throw new InvalidChemicalStringException(String.format("Unexpected ')' in string at position %d", i));
                    tmp = ChemicalPart.fromString(s.substring(chemicalPartStart, i));
                    chemicalPartStart = -1;
                    break;
                default:
                    if (Character.isDigit(s.charAt(i)) && chemicalPartStart == -1) {
                        num.append(s.charAt(i));
                    }
            }
        }
        if (chemicalPartStart != -1) throw new InvalidChemicalStringException(String.format("Unexpected '(' in string at position %d", s.length() - 1));
        if (tmp != null) {
            if (!Objects.equals(num.toString(), "")) out.add(tmp, Integer.parseInt(num.toString()));
            else out.add(tmp);
        }
        return out;
    }

    public Map<ChemicalPart, Integer> getContents() {
        return Map.copyOf(contents);
    }

    public Chemical add(ChemicalPart part) {
        this.contents.put(part, 1);
        return this;
    }

    public Chemical add(ChemicalPart part, int amount) {
        this.contents.put(part, amount);
        return this;
    }

    @Override
    public String toString() {
        if (this.contents.size() == 1) {
            for (ChemicalPart c : this.contents.keySet()) if (contents.get(c) == 1) return c.toString();
        }
        StringBuilder s = new StringBuilder();
        for (ChemicalPart c : this.contents.keySet()) {
            s.append("(").append(c.toString()).append(")");
            if (contents.get(c) > 1) s.append(contents.get(c).toString());
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chemical chemical = (Chemical) o;
        return com.google.common.base.Objects.equal(contents, chemical.contents);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(contents);
    }
}
