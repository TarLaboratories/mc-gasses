package org.tarlaboratories.tartech.chemistry;

import com.mojang.serialization.Codec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

public class Chemical {
    public static Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("unused")
    public enum Type {
        ACID,
        AMPHOTERIC,
        BINARY,
        HYDROXIDE,
        METAL,
        NOBLE_GAS,
        NON_METAL,
        OXIDE,
        SALT
    }

    /**
     * Represents the properties of a chemical
     * @param type the chemical's type (acid, salt, oxide, etc.)
     * @param crystallizationTemperature the temperature below which the chemical turns solid (if applicable)
     * @param boilingTemperature the temperature above which the chemical turns into gas (if applicable)
     * @param canBeGas if the chemical can turn into gas at all
     * @param canBeSolid if the chemical can turn solid at all
     * @param c the chemical's heat capacity (heat = c*m*dt)
     * @param r the chemical's electric resistance in mcOm*mm2/m (R = r*l/S)
     */
    public record Properties(Type type, double crystallizationTemperature, double boilingTemperature,
                             boolean canBeGas, boolean canBeSolid, double c, double r, int color) {
    }
    public static final Map<Chemical, Chemical.Properties> CHEMICAL_PROPERTIES = Map.of(
            Chemical.fromString("N2"), new Properties(Type.NON_METAL, 0, -196, true, false, 275, 0, 0xFFFFFF),
            Chemical.fromString("O2"), new Properties(Type.NON_METAL, 0, -182.98, true, false, 0.92, 0, 0x0000FF),
            Chemical.fromString("He"), new Properties(Type.NOBLE_GAS, 0, -268.93, true, false, 5.193, 0, 0xFFFFFF),
            Chemical.fromString("Fe"), new Properties(Type.METAL, 1538, 0, false, true, 0.64, 0.098, 0xFF8800),
            Chemical.fromString("(H2)(SO4)"), new Properties(Type.ACID, 0, 0, false, false, 0, 0, 0xFFFF00),
            Chemical.fromString("SO2"), new Properties(Type.OXIDE, 0, 0, false, false, 0.607, 0, 0xFFBB00),
            Chemical.fromString("(TeST)(GaS)"), new Properties(Type.BINARY, -20000, -10000, true, false, 1, 0, 0x00FFFF),
            Chemical.fromString("H2O"), new Properties(Type.BINARY, 0, 100, true, true, 4.2, 0, 0x0000BB),
            Chemical.fromString("CO2"), new Properties(Type.OXIDE, 0, -10000, true, false, 0.846, 0, 0x888888)
    );
    public static final Chemical OXYGEN = Chemical.fromString("O2");
    public static final Chemical SULFUR_DIOXIDE = Chemical.fromString("SO2");

    public static final Codec<Chemical> CODEC = Codec.STRING.xmap(Chemical::fromString, Chemical::toString);

    public static <T> @NotNull Map<Chemical, T> forEachChemical(BiFunction<Chemical, Chemical.Properties, T> function) {
        Map<Chemical, T> output = new HashMap<>();
        for (Chemical chemical : CHEMICAL_PROPERTIES.keySet()) {
            output.put(chemical, function.apply(chemical, CHEMICAL_PROPERTIES.get(chemical)));
        }
        return output;
    }

    public static Properties getProperties(Chemical chemical) {
        if (!CHEMICAL_PROPERTIES.containsKey(chemical)) LOGGER.warn("Attempt to access properties of a chemical that doesn't exist: {}", chemical.toString());
        return CHEMICAL_PROPERTIES.get(chemical);
    }

    public Properties getProperties() {
        return getProperties(this);
    }

    public Map<ChemicalPart, Integer> contents;

    public Chemical() {
        contents = new HashMap<>();
    }

    public Chemical(ChemicalPart @NotNull ... a) {
        contents = new HashMap<>();
        for (ChemicalPart i : a) contents.put(i, 1);
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

    @SuppressWarnings("UnusedReturnValue")
    public Chemical add(ChemicalPart part) {
        this.contents.put(part, 1);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
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

    public String toIdentifierString() {
        if (this.contents.size() == 1) {
            for (ChemicalPart c : this.contents.keySet()) if (contents.get(c) == 1) return c.toString().toLowerCase();
        }
        StringBuilder s = new StringBuilder();
        for (ChemicalPart c : this.contents.keySet()) {
            s.append("_").append(c.toString().toLowerCase()).append("_");
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
