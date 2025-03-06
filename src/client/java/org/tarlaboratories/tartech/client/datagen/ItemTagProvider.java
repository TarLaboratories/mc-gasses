package org.tarlaboratories.tartech.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import org.tarlaboratories.tartech.items.ModItems;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends FabricTagProvider<Item> {
    public ItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ITEM, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        FabricTagBuilder builder = getOrCreateTagBuilder(ModItems.CHEMICAL_FLUID_BUCKET_TAG);
        for (Chemical chemical : ModItems.CHEMICAL_FLUID_BUCKETS.keySet()) {
            builder.add(ModItems.CHEMICAL_FLUID_BUCKETS.get(chemical));
        }
        FabricTagBuilder debug_builder = getOrCreateTagBuilder(ModItems.DEBUG_TAG);
        debug_builder.add(ModItems.TEST_ITEM, ModItems.GAS_TEST_ITEM, Items.DEBUG_STICK);
    }
}
