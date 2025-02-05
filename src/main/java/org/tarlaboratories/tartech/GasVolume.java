package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.HashMap;
import java.util.Map;

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

    public GasVolume() {
        volume = 0;
        total_gas = 0;
        radioactivity = 0;
        contents = new HashMap<>();
    }

    public GasVolume(int volume, double total_gas, double radioactivity, double temperature, Map<Chemical, Double> contents) {
        this.volume = volume;
        this.total_gas = total_gas;
        this.radioactivity = radioactivity;
        this.temperature = temperature;
        this.contents = new HashMap<>(contents);
    }

    public GasVolume addGas(Chemical c, double amount) {
        total_gas += amount;
        if (contents.containsKey(c))
            contents.put(c, contents.get(c) + amount);
        else
            contents.put(c, amount);
        return this;
    }

    public double removeGas(Chemical c, double amount) {
        if (contents.containsKey(c) && contents.get(c) >= amount) {
            contents.put(c, contents.get(c) - amount);
            total_gas -= amount;
            return amount;
        } else if (contents.containsKey(c)) {
            double tmp = contents.get(c);
            contents.remove(c);
            total_gas -= tmp;
            return tmp;
        } else return 0;
    }

    public GasVolume multiplyContentsBy(double k) {
        this.contents.replaceAll((g, v) -> v * k);
        this.total_gas *= k;
        return this;
    }

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

    public HashMap<Chemical, Double> getContents() {
        return contents;
    }

    public GasVolume addVolume(int volume) {
        this.volume += volume;
        return this;
    }

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

    public void substractGasVolume(@NotNull GasVolume gasVolInPos) {
        this.volume -= gasVolInPos.getVolume();
        for (Chemical gas : this.getContents().keySet()) {
            this.removeGas(gas, gasVolInPos.getGasAmount(gas));
        }
    }
}
