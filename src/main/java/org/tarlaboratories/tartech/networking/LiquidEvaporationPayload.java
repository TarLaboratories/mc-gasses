package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record LiquidEvaporationPayload(BlockPos pos) implements CustomPayload {
    public static final Id<LiquidEvaporationPayload> ID = new Id<>(ModPayloads.LIQUID_EVAPORATION);
    public static final PacketCodec<RegistryByteBuf, LiquidEvaporationPayload> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, LiquidEvaporationPayload::pos, LiquidEvaporationPayload::new);

    @Override
    public Id<LiquidEvaporationPayload> getId() {
        return ID;
    }
}
