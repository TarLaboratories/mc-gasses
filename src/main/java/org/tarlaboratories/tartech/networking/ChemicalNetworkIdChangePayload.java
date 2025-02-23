package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record ChemicalNetworkIdChangePayload(BlockPos pos, int new_id) implements CustomPayload {
    public static final Id<ChemicalNetworkIdChangePayload> ID = new Id<>(ModPayloads.CHEMICAL_NETWORK_ID_CHANGE);
    public static final PacketCodec<RegistryByteBuf, ChemicalNetworkIdChangePayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ChemicalNetworkIdChangePayload::pos,
            PacketCodecs.INTEGER, ChemicalNetworkIdChangePayload::new_id,
            ChemicalNetworkIdChangePayload::new
    );

    @Override
    public Id<ChemicalNetworkIdChangePayload> getId() {
        return ID;
    }
}
