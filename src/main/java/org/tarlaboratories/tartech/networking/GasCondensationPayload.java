package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record GasCondensationPayload(BlockPos pos, String gas) implements CustomPayload {
    public static final Id<GasCondensationPayload> ID = new Id<>(ModPayloads.GAS_CONDENSATION);
    public static final PacketCodec<RegistryByteBuf, GasCondensationPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, GasCondensationPayload::pos,
            PacketCodecs.STRING, GasCondensationPayload::gas,
            GasCondensationPayload::new);

    @Override
    public Id<GasCondensationPayload> getId() {
        return ID;
    }
}
