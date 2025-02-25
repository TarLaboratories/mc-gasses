package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record GasVolumeDataRequestPayload(BlockPos pos) implements CustomPayload {
    public static final Id<GasVolumeDataRequestPayload> ID = new Id<>(ModPayloads.GAS_VOLUME_DATA_REQUEST);
    public static final PacketCodec<RegistryByteBuf, GasVolumeDataRequestPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, GasVolumeDataRequestPayload::pos,
            GasVolumeDataRequestPayload::new
    );

    @Override
    public Id<GasVolumeDataRequestPayload> getId() {
        return ID;
    }
}
