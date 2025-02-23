package org.tarlaboratories.tartech.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.ChemicalNetwork;
import org.tarlaboratories.tartech.StateSaverAndLoader;
import org.tarlaboratories.tartech.Tartech;

public class ModPayloads {
    public static final Identifier LIQUID_EVAPORATION = Identifier.of(Tartech.MOD_ID, "liquid_evaporation_payload");
    public static final Identifier GAS_CONDENSATION = Identifier.of(Tartech.MOD_ID, "gas_condensation_payload");
    public static final Identifier CHEMICAL_NETWORK_ID_CHANGE = Identifier.of(Tartech.MOD_ID, "chemical_network_id_change");
    public static final Identifier CHEMICAL_NETWORK_DATA_REQUEST = Identifier.of(Tartech.MOD_ID, "chemical_network_data_request");
    public static final Identifier CHEMICAL_NETWORK_DATA = Identifier.of(Tartech.MOD_ID, "chemical_network_data");

    public static void registerNetworkingReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ChemicalNetworkDataRequestPayload.ID, (payload, context) -> {
            if (!context.player().isCreative()) return;
            ChemicalNetwork response = StateSaverAndLoader.getWorldState(context.player().getServerWorld()).getChemicalNetwork(payload.chemical_network_id());
            if (response != null) ServerPlayNetworking.send(context.player(), new ChemicalNetworkDataPayload(response, payload.chemical_network_id()));
        });
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(LiquidEvaporationPayload.ID, LiquidEvaporationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GasCondensationPayload.ID, GasCondensationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChemicalNetworkIdChangePayload.ID, ChemicalNetworkIdChangePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChemicalNetworkDataRequestPayload.ID, ChemicalNetworkDataRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChemicalNetworkDataPayload.ID, ChemicalNetworkDataPayload.CODEC);
        registerNetworkingReceivers();
    }
}
