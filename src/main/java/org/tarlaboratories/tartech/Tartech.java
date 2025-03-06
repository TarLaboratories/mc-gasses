package org.tarlaboratories.tartech;

import net.fabricmc.api.ModInitializer;
import org.tarlaboratories.tartech.blocks.ModBlocks;
import org.tarlaboratories.tartech.fluids.ModFluids;
import org.tarlaboratories.tartech.items.ModItems;
import org.tarlaboratories.tartech.networking.ModPayloads;

public class Tartech implements ModInitializer {
    public static final String MOD_ID = "tartech";

    @Override
    public void onInitialize() {
        ModComponents.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModFluids.initialize();
        ModEntities.initialize();
        ModCommands.initialize();
        ModEventListeners.initialize();
        ModPayloads.initialize();
    }
}
