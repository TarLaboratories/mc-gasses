package org.tarlaboratories.tartech.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.tarlaboratories.tartech.ChemicalNetwork;

public record ChemicalNetworkDataPayload(ChemicalNetwork data, int network_id) implements CustomPayload {
    public static final Id<ChemicalNetworkDataPayload> ID = new Id<>(ModPayloads.CHEMICAL_NETWORK_DATA);

    public static final PacketCodec<RegistryByteBuf, ChemicalNetworkDataPayload> CODEC = PacketCodec.tuple(
            ChemicalNetwork.PACKET_CODEC, ChemicalNetworkDataPayload::data,
            PacketCodecs.INTEGER, ChemicalNetworkDataPayload::network_id,
            ChemicalNetworkDataPayload::new
    );

    public Id<ChemicalNetworkDataPayload> getId() {
        return ID;
    }
}
