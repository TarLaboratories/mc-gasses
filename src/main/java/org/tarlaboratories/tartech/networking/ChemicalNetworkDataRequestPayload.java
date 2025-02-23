package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ChemicalNetworkDataRequestPayload(int chemical_network_id) implements CustomPayload {
    public static final Id<ChemicalNetworkDataRequestPayload> ID = new Id<>(ModPayloads.CHEMICAL_NETWORK_DATA_REQUEST);
    public static final PacketCodec<RegistryByteBuf, ChemicalNetworkDataRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ChemicalNetworkDataRequestPayload::chemical_network_id,
            ChemicalNetworkDataRequestPayload::new
    );

    @Override
    public Id<ChemicalNetworkDataRequestPayload> getId() {
        return ID;
    }
}
