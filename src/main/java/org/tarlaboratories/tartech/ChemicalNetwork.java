package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.gas.GasVolume;

import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public class ChemicalNetwork {
    public static final Codec<ChemicalNetwork> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            GasVolume.CODEC.fieldOf("contents").forGetter(ChemicalNetwork::getContents)
    ).apply(instance, ChemicalNetwork::new));

    public static final PacketCodec<RegistryByteBuf, ChemicalNetwork> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    protected GasVolume contents;

    public ChemicalNetwork() {
        this.contents = new GasVolume(false);
    }

    public ChemicalNetwork(@NotNull GasVolume contents) {
        this.contents = contents.copy();
        this.contents.disableLiquidCheck();
    }

    public void addChemical(Chemical chemical, double amount, double temperature) {
        GasVolume tmp = new GasVolume();
        tmp.setTemperature(temperature);
        tmp.addGas(chemical, amount);
        this.contents.mergeWith(tmp);
    }

    public GasVolume getPart(int volume) {
        return this.getFilteredPart(volume, (chemical, amount) -> true);
    }

    public GasVolume getFilteredPart(int volume, BiPredicate<Chemical, Double> filter) {
        GasVolume tmp = this.contents.getPart(volume);
        GasVolume out = tmp.copy().multiplyContentsBy(0);
        for (Chemical chemical : this.contents.getContents().keySet()) {
            if (filter.test(chemical, this.contents.getGasAmount(chemical))) {
                out.addGas(chemical, tmp.getGasAmount(chemical));
            }
        }
        return out;
    }

    public void addVolume(int volume) {
        this.contents.addVolume(volume);
    }

    public double getPressure() {
        return this.contents.getPressure();
    }

    public double getTemperature() {
        return this.contents.getTemperature();
    }

    protected GasVolume getContents() {
        return this.contents;
    }

    public void mergeWith(@NotNull ChemicalNetwork other) {
        this.contents.mergeWith(other.getContents());
    }

    public void multiplyContentsBy(double k) {
        this.contents.multiplyContentsBy(k);
    }

    public Text getInfo() {
        return this.contents.getInfo(true);
    }

    public double getVolume() {
        return this.contents.getVolume();
    }

    public void mergeContentsWith(GasVolume other) {
        this.contents.mergeWith(other);
    }
}
