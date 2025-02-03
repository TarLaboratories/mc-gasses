package org.tarlaboratories.tartech.chemistry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashSet;
import java.util.List;

public class Chemical {
    public static final Chemical HYDROGEN = primitiveOf("H");
    public static final Chemical OXYGEN = primitiveOf("O");
    public static final Chemical WATER = new Chemical(ChemicalPart.WATER);
    public static final Chemical SULFUR_DIOXIDE = new Chemical(ChemicalPart.SULFUR_DIOXIDE);

    public static final Codec<Chemical> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ChemicalPart.CODEC).fieldOf("contents").forGetter(Chemical::getContents)
    ).apply(instance, Chemical::new));

    public static Chemical primitiveOf(String element) {
        return new Chemical(ChemicalPart.primitiveOf(element));
    }
    public HashSet<ChemicalPart> contents;

    public Chemical() {
        contents = new HashSet<>();
    }

    public Chemical(ChemicalPart... a) {
        contents = new HashSet<>();
        contents.addAll(List.of(a));
    }

    public Chemical(List<ChemicalPart> a) {
        contents = new HashSet<>(a);
    }

    public List<ChemicalPart> getContents() {
        return List.copyOf(contents);
    }

    public Chemical add(ChemicalPart part) {
        this.contents.add(part);
        return this;
    }

    @Override
    public String toString() {
        if (this.contents.size() == 1) {
            for (ChemicalPart c : this.contents) return c.toString();
        }
        StringBuilder s = new StringBuilder();
        for (ChemicalPart c : this.contents) {
            s.append("(").append(c.toString()).append(")");
        }
        return s.toString();
    }
}
