package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlayerData {
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("air").forGetter(PlayerData::getAir)
    ).apply(instance, PlayerData::new));

    public PlayerData(int air) {
        this.air = air;
    }

    public PlayerData() {
        this.air = 300;
    }

    public int air;

    public int getAir() {
        return air;
    }
}
