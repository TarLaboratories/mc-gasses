package org.tarlaboratories.tartech;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the state of a volume of gas.
 * Contains information about all gases and their amounts, temperature, radioactivity and volume.
 * Does NOT contain information about any positions, worlds or blocks.
 */
public class GasVolume {
    protected int volume;
    protected double total_gas;
    protected double radioactivity;
    protected double temperature;
    protected HashMap<Chemical, Double> contents;

    public static final Codec<GasVolume> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("volume").forGetter(GasVolume::getVolume),
            Codec.DOUBLE.fieldOf("total_gas").forGetter(GasVolume::getTotalGas),
            Codec.DOUBLE.fieldOf("radioactivity").forGetter(GasVolume::getRadioactivity),
            Codec.DOUBLE.fieldOf("temperature").forGetter(GasVolume::getTemperature),
            Codec.unboundedMap(Chemical.CODEC, Codec.DOUBLE).fieldOf("contents").forGetter(GasVolume::getContents)
    ).apply(instance, GasVolume::new));

    /**
     * Creates a {@code GasVolume} that has {@code volume} and {@code radioactivity} set to {@code 0}, and no contents.
     */
    public GasVolume() {
        volume = 0;
        total_gas = 0;
        radioactivity = 0;
        contents = new HashMap<>();
    }

    /**
     * Creates a {@code GasVolume} that has all of its parameters set to the arguments of this constructor
     */
    public GasVolume(int volume, double total_gas, double radioactivity, double temperature, Map<Chemical, Double> contents) {
        this.volume = volume;
        this.total_gas = total_gas;
        this.radioactivity = radioactivity;
        this.temperature = temperature;
        this.contents = new HashMap<>(contents);
    }

    /**
     * Adds the specified gas to this volume
     * @param gas the gas type to add (represented by a {@code Chemical})
     * @param amount the amount of this gas to add (should be positive)
     * @return this {@code GasVolume}
     * @see GasVolume#removeGas
     */
    public GasVolume addGas(Chemical gas, double amount) {
        total_gas += amount;
        if (contents.containsKey(gas))
            contents.put(gas, contents.get(gas) + amount);
        else
            contents.put(gas, amount);
        return this;
    }

    /**
     * Removes the specified gas from this volume, if {@code amount} is more than the current one,
     * deletes this gas completely
     * @param gas the gas type to remove (represented by a {@code Chemical})
     * @param amount the amount of this gas to remove (should be positive)
     * @see GasVolume#addGas
     */
    public void removeGas(Chemical gas, double amount) {
        if (contents.containsKey(gas) && contents.get(gas) >= amount) {
            contents.put(gas, contents.get(gas) - amount);
            total_gas -= amount;
        } else if (contents.containsKey(gas)) {
            double tmp = contents.get(gas);
            contents.remove(gas);
            total_gas -= tmp;
        }
    }

    /**
     * Multiplies all gas amounts in this volume by {@code k}
     * @param k the coefficient to multiply all gases by
     * @return this {@code GasVolume}
     */
    public GasVolume multiplyContentsBy(double k) {
        this.contents.replaceAll((g, v) -> v * k);
        this.total_gas *= k;
        return this;
    }

    /**
     * @return the pressure in this gas volume, calculated as {@code total_gas_amount/volume}
     */
    public double getPressure() {
        return total_gas/volume;
    }

    public double getTemperature() {
        return temperature;
    }

    public GasVolume setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public double getRadioactivity() {
        return radioactivity;
    }

    public GasVolume setRadioactivity(double r) {
        radioactivity = r;
        return this;
    }

    /**
     * @return the sum of amounts of every gas in this volume
     */
    public double getTotalGas() {
        return total_gas;
    }

    public double getGasAmount(Chemical gas) {
        if (contents.containsKey(gas)) return contents.get(gas);
        return 0;
    }

    public int getVolume() {
        return volume;
    }

    /**
     * @return a {@code Map}, where for each gas (represented by {@code Chemical}) the value is it's amount
     */
    public Map<Chemical, Double> getContents() {
        return contents;
    }

    public GasVolume addVolume(int volume) {
        this.volume += volume;
        return this;
    }

    /**
     * Adds all the contents of {@code other} to this volume,
     * {@code temperature} and {@code radioactivity} are the average between the two volumes,
     * and the {@code volume} is the sum of volumes of the two objects.
     * Does NOT change the {@code other} volume in any way.
     * @param other the {@code GasVolume} to merge with
     */
    public void mergeWith(@NotNull GasVolume other) {
        this.volume += other.getVolume();
        this.radioactivity = (this.radioactivity + other.radioactivity)/2;
        this.temperature = (this.temperature + other.temperature)/2;
        for (Chemical gas : other.getContents().keySet()) {
            this.addGas(gas, other.getContents().get(gas));
        }
    }

    @Contract("_ -> new")
    public static @NotNull GasVolume copyOf(@NotNull GasVolume gasVolume) {
        return new GasVolume(gasVolume.getVolume(), gasVolume.getTotalGas(), gasVolume.getRadioactivity(), gasVolume.getTemperature(), new HashMap<>(gasVolume.getContents()));
    }

    public GasVolume copy() {
        return copyOf(this);
    }

    /**
     * @param size the size of the part to get
     * @return a new {@code GasVolume} that has {@code volume} and all contents multiplied by {@code size/original_volume}, {@code temperature} and {@code radioactivity} stay the same.
     */
    public GasVolume getPart(int size) {
        GasVolume out = new GasVolume();
        out.temperature = temperature;
        out.radioactivity = radioactivity;
        out.volume = size;
        for (Chemical gas : this.getContents().keySet()) {
            out.addGas(gas, this.getGasAmount(gas)*size/this.getVolume());
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GasVolume gasVolume = (GasVolume) o;
        return volume == gasVolume.volume && Double.compare(total_gas, gasVolume.total_gas) == 0 && Double.compare(radioactivity, gasVolume.radioactivity) == 0 && Double.compare(temperature, gasVolume.temperature) == 0 && Objects.equal(contents, gasVolume.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(volume, total_gas, radioactivity, temperature, contents);
    }
}
