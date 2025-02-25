package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.tarlaboratories.tartech.gas.GasVolume;

public record GasVolumeDataPayload(GasVolume volume) implements CustomPayload {
    public static final Id<GasVolumeDataPayload> ID = new Id<>(ModPayloads.GAS_VOLUME_DATA);
    public static final PacketCodec<RegistryByteBuf, GasVolumeDataPayload> CODEC = PacketCodec.tuple(
            GasVolume.PACKET_CODEC, GasVolumeDataPayload::volume,
            GasVolumeDataPayload::new
    );

    public Id<GasVolumeDataPayload> getId() {
        return ID;
    }
}
