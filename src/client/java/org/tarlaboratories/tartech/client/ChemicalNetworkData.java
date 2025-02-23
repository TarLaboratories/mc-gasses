package org.tarlaboratories.tartech.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.ChemicalNetwork;
import org.tarlaboratories.tartech.networking.ChemicalNetworkDataPayload;
import org.tarlaboratories.tartech.networking.ChemicalNetworkDataRequestPayload;

import java.util.HashMap;
import java.util.Map;

public class ChemicalNetworkData {
    private static final Map<Integer, ChemicalNetwork> data = new HashMap<>();
    private static final Map<Integer, Long> waiting = new HashMap<>();

    public static @Nullable ChemicalNetwork getOrRequest(int id) {
        if (MinecraftClient.getInstance().world == null) return null;
        long time = MinecraftClient.getInstance().world.getTime();
        if (waiting.containsKey(id) && waiting.get(id) + 20 > time) return data.get(id);
        else if (waiting.containsKey(id) && waiting.get(id) + 40 < time) data.remove(id);
        ClientPlayNetworking.send(new ChemicalNetworkDataRequestPayload(id));
        return data.get(id);
    }

    public static void receivePayload(@NotNull ChemicalNetworkDataPayload payload, ClientPlayNetworking.Context ignoredContext) {
        data.put(payload.network_id(), payload.data());
        if (MinecraftClient.getInstance().world != null)
            waiting.put(payload.network_id(), MinecraftClient.getInstance().world.getTime());
    }
}
