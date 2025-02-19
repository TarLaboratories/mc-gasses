package org.tarlaboratories.tartech.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.Tartech;

public class ModPayloads {
    public static final Identifier LIQUID_EVAPORATION = Identifier.of(Tartech.MOD_ID, "liquid_evaporation_payload");
    public static final Identifier GAS_CONDENSATION = Identifier.of(Tartech.MOD_ID, "gas_condensation_payload");

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(LiquidEvaporationPayload.ID, LiquidEvaporationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GasCondensationPayload.ID, GasCondensationPayload.CODEC);
    }
}
