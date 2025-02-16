package org.tarlaboratories.tartech;

import net.fabricmc.api.ModInitializer;
import org.tarlaboratories.tartech.networking.ModPayloads;

public class Tartech implements ModInitializer {
    public static final String MOD_ID = "tartech";

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModBlocks.initialize();
        ModFluids.initialize();
        ModEntities.initialize();
        ModCommands.initialize();
        ModEventListeners.initialize();
        ModPayloads.initialize();
    }
}
