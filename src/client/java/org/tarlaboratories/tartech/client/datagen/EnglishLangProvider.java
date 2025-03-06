package org.tarlaboratories.tartech.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.blocks.ModBlocks;
import org.tarlaboratories.tartech.items.ModItems;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {
    protected EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, @NotNull TranslationBuilder translationBuilder) {
        Chemical.forEachChemical((chemical, properties) -> {
            translationBuilder.add(ModItems.CHEMICAL_FLUID_BUCKETS.get(chemical), String.format("Bucket of %s", chemical.toString()));
            return null;
        });
        translationBuilder.add(ModItems.TEST_ITEM, "Debug Chunk Reinitializator 3000");
        translationBuilder.add(ModItems.GAS_ANALYSER_ITEM, "Gas Analyzer");
        translationBuilder.add(ModItems.GAS_TEST_ITEM, "Debug Chunk Updater 3000");
        translationBuilder.add(ModItems.MAIN_ITEM_GROUP_KEY, "TarTech");
        translationBuilder.add(ModBlocks.PIPE.asItem(), "Pipe");
        translationBuilder.add(ModBlocks.PIPE, "Pipe");
        translationBuilder.add(ModBlocks.FULL_PIPE.asItem(), "Full Pipe");
        translationBuilder.add(ModBlocks.FULL_PIPE, "Full Pipe");
        translationBuilder.add(ModBlocks.PIPE_OPENING.asItem(), "Pipe Opening");
        translationBuilder.add(ModBlocks.PIPE_OPENING, "Pipe Opening");
        translationBuilder.add("tartech.gas.info", "Gas info:");
        translationBuilder.add("tartech.gas.temperature", "Temperature:");
        translationBuilder.add("tartech.gas.pressure", "Pressure:");
        translationBuilder.add("tartech.gas.volume", "Volume:");
        translationBuilder.add("tartech.gas.gas_info", "Gas present in this volume:");
        translationBuilder.add("tartech.gas.no_gas_info", "There is no gas present in this volume");
        translationBuilder.add("tartech.gas.c", "C: ");
        translationBuilder.add("tartech.gas.fluid_info", "Fluids present in this volume:");
        translationBuilder.add("tartech.gas.no_fluid_info", "There are no fluids present in this volume");
        translationBuilder.add("tartech.gas.fluid_info_not_available", "Information about fluids present in this volume is not available");
        translationBuilder.add("tartech.gas.total", "Total gas:");
        translationBuilder.add("tartech.gas.fluid_total", "Total fluid:");
        translationBuilder.add("tartech.gas.exposed", "Is exposed to atmosphere:");
        translationBuilder.add("tartech.yes", "Yes");
        translationBuilder.add("tartech.no", "No");
        translationBuilder.add("tag.item.tartech.chemical_fluid_buckets", "Buckets of chemicals");
        translationBuilder.add("tag.item.tartech.debug", "Debug items");
        translationBuilder.add("death.attack.no_air", "%s couldn't breathe");
    }
}
