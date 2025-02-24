package org.tarlaboratories.tartech.gas;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the state of a volume of gas.
 * Contains information about all gases and their amounts, temperature, radioactivity and volume.
 * Does NOT contain information about any positions, worlds or blocks.
 */
public class GasVolume {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();
    protected int volume;
    protected double total_gas, total_liquid;
    protected double radioactivity;
    protected double temperature;
    protected double total_c;
    protected boolean is_exposed;
    protected HashMap<Chemical, Double> contents, liquid_contents;
    protected Set<Chemical> to_be_liquefied;
    protected boolean do_liquid_check;
    public static final Map<Chemical, Range<Double>> breathable_req = Map.of(
            Chemical.OXYGEN, new Range<>(0.1, 0.3),
            Chemical.fromString("CO2"), new Range<>(0., 0.06)
    );
    public static final Range<Double> breathable_pressure_req = new Range<>(0.5, Double.POSITIVE_INFINITY);

    protected void checkForLiquids() {
        if (!do_liquid_check) return;
        for (Chemical gas : contents.keySet()) {
            if (gas.getProperties().boilingTemperature() > temperature) to_be_liquefied.add(gas);
        }
        to_be_liquefied.removeIf((liquid) -> liquid.getProperties().boilingTemperature() <= temperature);
    }

    public static final Codec<GasVolume> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("volume").forGetter(GasVolume::getVolume),
            Codec.DOUBLE.fieldOf("total_gas").forGetter(GasVolume::getTotalGas),
            Codec.DOUBLE.fieldOf("total_liquid").forGetter(GasVolume::getTotalLiquid),
            Codec.DOUBLE.fieldOf("radioactivity").forGetter(GasVolume::getRadioactivity),
            Codec.DOUBLE.fieldOf("temperature").forGetter(GasVolume::getTemperature),
            Codec.DOUBLE.fieldOf("total_c").forGetter(GasVolume::getTotalC),
            Codec.BOOL.fieldOf("is_exposed").forGetter(GasVolume::isExposed),
            Codec.unboundedMap(Chemical.CODEC, Codec.DOUBLE).fieldOf("contents").forGetter(GasVolume::getContents),
            Codec.unboundedMap(Chemical.CODEC, Codec.DOUBLE).fieldOf("liquid_contents").forGetter(GasVolume::getLiquidContents)
    ).apply(instance, GasVolume::new));

    @SuppressWarnings("unused")
    public static final PacketCodec<RegistryByteBuf, GasVolume> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    public Boolean isExposed() {
        return this.is_exposed;
    }

    public Double getTotalLiquid() {
        return this.total_liquid;
    }

    private Map<Chemical, Double> getLiquidContents() {
        return this.liquid_contents;
    }

    /**
     * Creates a {@code GasVolume} that has {@code volume} and {@code radioactivity} set to {@code 0}, and no contents.
     */
    public GasVolume() {
        volume = 0;
        total_gas = 0;
        total_liquid = 0;
        radioactivity = 0;
        total_c = 0;
        is_exposed = false;
        contents = new HashMap<>();
        liquid_contents = new HashMap<>();
        to_be_liquefied = new HashSet<>();
        do_liquid_check = true;
    }

    public GasVolume(boolean do_liquid_check) {
        volume = 0;
        total_gas = 0;
        total_liquid = 0;
        radioactivity = 0;
        total_c = 0;
        is_exposed = false;
        contents = new HashMap<>();
        liquid_contents = new HashMap<>();
        to_be_liquefied = new HashSet<>();
        this.do_liquid_check = do_liquid_check;
    }

    /**
     * Creates a {@code GasVolume} that has all of its parameters set to the arguments of this constructor
     */
    public GasVolume(int volume, double total_gas, double total_liquid, double radioactivity, double temperature, double total_c, boolean is_exposed, Map<Chemical, Double> contents, Map<Chemical, Double> liquid_contents) {
        this.volume = volume;
        this.total_gas = total_gas;
        this.total_liquid = total_liquid;
        this.radioactivity = radioactivity;
        this.temperature = temperature;
        this.total_c = total_c;
        this.to_be_liquefied = new HashSet<>();
        this.is_exposed = is_exposed;
        this.contents = new HashMap<>(contents);
        this.liquid_contents = new HashMap<>(liquid_contents);
        this.do_liquid_check = true;
    }

    /**
     * Adds the specified gas to this volume
     * @param gas the gas type to add (represented by a {@code Chemical})
     * @param amount the amount of this gas to add (should be positive)
     * @return this {@code GasVolume}
     * @see GasVolume#removeGas
     */
    public GasVolume addGas(@NotNull Chemical gas, double amount) {
        total_gas += amount;
        total_c += gas.getProperties().c()*amount;
        if (contents.containsKey(gas))
            contents.put(gas, contents.get(gas) + amount);
        else
            contents.put(gas, amount);
        checkForLiquids();
        return this;
    }

    /**
     * Removes the specified gas from this volume, if {@code amount} is more than the current one,
     * deletes this gas completely
     * @param gas the gas type to remove (represented by a {@code Chemical})
     * @param amount the amount of this gas to remove (should be positive)
     * @see GasVolume#addGas
     */
    public double removeGas(Chemical gas, double amount) {
        if (contents.containsKey(gas) && contents.get(gas) >= amount) {
            contents.put(gas, contents.get(gas) - amount);
            total_gas -= amount;
            total_c -= gas.getProperties().c()*amount;
            return amount;
        } else if (contents.containsKey(gas)) {
            double tmp = contents.get(gas);
            contents.remove(gas);
            total_gas -= tmp;
            total_c -= gas.getProperties().c()*tmp;
            return tmp;
        }
        return 0;
    }

    /**
     * Multiplies all gas amounts in this volume by {@code k}
     * @param k the coefficient to multiply all gases by
     * @return this {@code GasVolume}
     */
    public GasVolume multiplyContentsBy(double k) {
        this.contents.replaceAll((g, v) -> v * k);
        this.total_gas *= k;
        this.total_c *= k;
        return this;
    }

    /**
     * @return the pressure in this gas volume, calculated as {@code (total_gas_amount + total_liquid_amount)/volume}
     */
    public double getPressure() {
        return (total_gas + total_liquid)/volume;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getTotalC() {
        return total_c;
    }

    public GasVolume setTemperature(double temperature) {
        this.temperature = temperature;
        checkForLiquids();
        return this;
    }

    public void addHeat(double amount) {
        this.temperature += amount/total_c;
        checkForLiquids();
    }

    public void disableLiquidCheck() {
        this.do_liquid_check = false;
    }

    public Pair<Chemical, Double> getLiquidToBeLiquefied() {
        this.checkForLiquids();
        for (Chemical liquid : this.to_be_liquefied) {
            double amount = this.removeGas(liquid, 1);
            this.total_c += liquid.getProperties().c()*amount;
            if (!this.contents.containsKey(liquid)) this.to_be_liquefied.remove(liquid);
            this.liquid_contents.put(liquid, this.liquid_contents.getOrDefault(liquid, 0d) + amount);
            this.total_liquid += amount;
            return new Pair<>(liquid, amount);
        }
        return new Pair<>(null, 0d);
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
        return contents.getOrDefault(gas, 0.);
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
        this.is_exposed = this.is_exposed || other.is_exposed;
        if (this.total_c + other.total_c != 0)
            this.temperature = (this.temperature*this.total_c + other.temperature*other.total_c)/(this.total_c + other.total_c);
        else if (this.volume != 0)
            this.temperature = (this.temperature*(this.volume - other.volume) + other.temperature*other.volume)/this.volume;
        this.total_c += other.total_c;
        for (Chemical gas : other.getContents().keySet()) {
            this.addGas(gas, other.getContents().get(gas));
        }
        checkForLiquids();
    }

    @Contract("_ -> new")
    public static @NotNull GasVolume copyOf(@NotNull GasVolume gasVolume) {
        return new GasVolume(gasVolume.getVolume(), gasVolume.getTotalGas(), gasVolume.getTotalLiquid(), gasVolume.getRadioactivity(), gasVolume.getTemperature(), gasVolume.getTotalC(), gasVolume.isExposed(), new HashMap<>(gasVolume.getContents()), new HashMap<>(gasVolume.liquid_contents));
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
        out.is_exposed = is_exposed;
        for (Chemical gas : this.getContents().keySet()) {
            out.addGas(gas, this.getGasAmount(gas)*size/this.getVolume());
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GasVolume gasVolume = (GasVolume) o;
        return volume == gasVolume.volume && Double.compare(total_gas, gasVolume.total_gas) == 0 && Double.compare(radioactivity, gasVolume.radioactivity) == 0 && Double.compare(temperature, gasVolume.temperature) == 0 && Objects.equal(contents, gasVolume.contents) && Objects.equal(liquid_contents, gasVolume.liquid_contents) && Boolean.compare(is_exposed, gasVolume.is_exposed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(volume, total_gas, radioactivity, temperature, contents, liquid_contents);
    }

    public void addLiquid(@NotNull FluidState fluid) {
        Chemical chemical = ((ChemicalFluid) fluid.getFluid()).getChemical();
        this.liquid_contents.put(chemical, this.liquid_contents.getOrDefault(chemical, 0d) + 1);
        this.total_liquid++;
        this.total_c += chemical.getProperties().c();
    }

    /**
     * @param extended whether to include information that typically is not available to survival players
     * @return a {@code Text} object containing human-readable information about this {@code GasVolume}
     */
    public Text getInfo(boolean extended) {
        boolean display_volumes = (this.volume <= 1000) || extended;
        MutableText out = Text.empty();
        out.append(Text.translatable("tartech.gas.info")).append("\n");
        out.append(Text.translatable("tartech.gas.temperature").append(String.format(" %f\n", temperature)));
        double delta = Math.min(getPressure() - breathable_pressure_req.minInclusive(), breathable_pressure_req.maxInclusive() - getPressure());
        if (!breathable_pressure_req.contains(getPressure())) delta = 0;
        out.append(Text.translatable("tartech.gas.pressure").append(Text.literal(String.format(" %f\n", getPressure())).formatted(double_to_format(2*delta/(breathable_pressure_req.maxInclusive() - breathable_pressure_req.minInclusive())))));
        if (display_volumes) {
            out.append(Text.translatable("tartech.gas.volume").append(String.format(" %d\n", volume)));
            if (contents.isEmpty()) out.append(Text.translatable("tartech.gas.no_gas_info")).append("\n");
            else {
                out.append(Text.translatable("tartech.gas.gas_info")).append("\n");
                for (Chemical gas : contents.keySet()) {
                    MutableText text = Text.literal(String.format("%s: %d mB (%.2f%%)\n", gas.toString(), Math.round(contents.get(gas)*1000), contents.get(gas)*100/volume));
                    double tmp = breathability(gas);
                    out.append(text.formatted(double_to_format(tmp)));
                }
                out.append(Text.translatable("tartech.gas.total")).append(String.format(" %d mB\n", Math.round(total_gas*1000)));
            }
        } else {
            out.append(Text.translatable("tartech.gas.volume").append(">1000 B\n"));
            if (contents.isEmpty()) out.append(Text.translatable("tartech.gas.no_gas_info")).append("\n");
            else out.append(Text.translatable("tartech.gas.gas_info")).append("\n");
            for (Chemical gas : contents.keySet()) {
                MutableText text = Text.literal(String.format("%s: %.2f%%\n", gas.toString(), contents.get(gas)*100/volume));
                double tmp = breathability(gas);
                out.append(text.formatted(double_to_format(tmp)));
            }
        }
        out.append(Text.translatable("tartech.gas.exposed")).append(" ").append(is_exposed ? Text.translatable("tartech.yes") : Text.translatable("tartech.no"));
        if (extended) {
            out.append("\n");
            MutableText extended_info = Text.empty();
            extended_info.append(Text.translatable("tartech.gas.c")).append(String.format(" %f\n", total_c));
            if (is_exposed) extended_info.append(Text.translatable("tartech.gas.fluid_info_not_available"));
            else if (liquid_contents.isEmpty()) extended_info.append(Text.translatable("tartech.gas.no_fluid_info"));
            else {
                extended_info.append(Text.translatable("tartech.gas.fluid_info")).append("\n");
                for (Chemical gas : liquid_contents.keySet()) extended_info.append(String.format("%s: %d mB (%.2f%%)\n", gas.toString(), Math.round(liquid_contents.get(gas)*1000), liquid_contents.get(gas)*100/volume));
                extended_info.append(Text.translatable("tartech.gas.fluid_total")).append(String.format(" %d mB", Math.round(total_liquid*1000)));
            }
            out.append(extended_info.formatted(Formatting.LIGHT_PURPLE));
        }
        return out;
    }

    public void evaporateLiquid(Chemical chemical, int amount) {
        this.total_liquid -= amount;
        this.liquid_contents.put(chemical, this.liquid_contents.getOrDefault(chemical, 0d) - amount);
        this.addGas(chemical, amount);
    }

    public GasVolume exposed() {
        GasVolume gasVolume = this.copy();
        gasVolume.is_exposed = true;
        return gasVolume;
    }

    public GasVolume unexposed() {
        this.is_exposed = false;
        return this;
    }

    public boolean breathable() {
        for (Chemical gas : breathable_req.keySet()) {
            if (breathability(gas) < 0.05) return false;
        }
        return breathable_pressure_req.contains(getPressure());
    }

    private double breathability(Chemical gas) {
        if (!breathable_req.containsKey(gas)) return Double.NaN;
        if (!breathable_req.get(gas).contains(contents.getOrDefault(gas, 0.)/volume)) return 0;
        double req_min = breathable_req.get(gas).minInclusive(), req_max = breathable_req.get(gas).maxInclusive(), req_range = req_max - req_min;
        double cur = contents.getOrDefault(gas, 0.)/volume;
        double delta = Math.min(cur - req_min, req_max - cur);
        if (Double.compare(req_min, 0.) == 0) delta = (req_max - cur)/2;
        return delta/(req_range/2);
    }

    private Formatting double_to_format(double tmp) {
        Formatting format = Formatting.GRAY;
        if (tmp < 0.05) format = Formatting.DARK_RED;
        else if (tmp < 0.1) format = Formatting.RED;
        else if (tmp < 0.3) format = Formatting.GOLD;
        else if (tmp < 0.7) format = Formatting.YELLOW;
        else if (tmp < 0.9) format = Formatting.GREEN;
        else if (tmp <= 1.0) format = Formatting.DARK_GREEN;
        return format;
    }
}
