package org.tarlaboratories.tartech.chemistry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Pair;

import java.util.HashSet;
import java.util.List;

public class ChemicalPart {
    public static final ChemicalPart HYDROGEN = primitiveOf("H");
    public static final ChemicalPart OXYGEN = primitiveOf("O");
    public static final ChemicalPart WATER = primitiveOf("H").addElement(ChemicalElement.OXYGEN, 1);

    public static final Codec<ChemicalPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.pair(ChemicalElement.CODEC, Codec.INT)).fieldOf("contents").forGetter(ChemicalPart::getContents)
    ).apply(instance, ChemicalPart::new));

    public static ChemicalPart primitiveOf(String element) {
        return (new ChemicalPart()).addElement(new ChemicalElement(element), 2);
    }

    public HashSet<Pair<ChemicalElement, Integer>> contents;

    public ChemicalPart() {
        this.contents = new HashSet<>();
    }

    public ChemicalPart(List<Pair<ChemicalElement, Integer>> contents) {
        this.contents = new HashSet<>(contents);
    }

    public ChemicalPart addElement(ChemicalElement element, int amount) {
        this.contents.add(new Pair<>(element, amount));
        return this;
    }

    public List<Pair<ChemicalElement, Integer>> getContents() {
        return List.copyOf(contents);
    }
}
