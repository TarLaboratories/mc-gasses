package org.tarlaboratories.tartech;

import net.fabricmc.api.ModInitializer;

public class Tartech implements ModInitializer {
    public static final String MOD_ID = "tartech";

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModBlocks.initialize();
        ModEntities.initialize();
        ModCommands.initialize();
        ModEventListeners.initialize();
    }
}
