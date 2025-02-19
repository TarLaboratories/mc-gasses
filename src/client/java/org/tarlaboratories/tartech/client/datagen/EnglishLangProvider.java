package org.tarlaboratories.tartech.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.ModItems;
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
        translationBuilder.add(ModItems.GAS_ANALYZER_ITEM, "Gas Analyzer");
        translationBuilder.add(ModItems.GAS_TEST_ITEM, "Debug Chunk Updater 3000");
        translationBuilder.add(ModItems.MAIN_ITEM_GROUP_KEY, "TarTech");
        translationBuilder.add("tartech.gas.info", "Gas info:");
        translationBuilder.add("tartech.gas.temperature", "Temperature:");
        translationBuilder.add("tartech.gas.pressure", "Pressure:");
        translationBuilder.add("tartech.gas.volume", "Volume:");
        translationBuilder.add("tartech.gas.gas_info", "Gas present in this volume:");
        translationBuilder.add("tartech.gas.no_gas_info", "There is no gas present in this volume");
        translationBuilder.add("tartech.gas.c", "C: ");
        translationBuilder.add("tartech.gas.fluid_info", "Fluids present in this volume:");
        translationBuilder.add("tartech.gas.no_fluid_info", "There are no fluids present in this volume");
        translationBuilder.add("tartech.gas.total", "Total gas:");
        translationBuilder.add("tartech.gas.fluid_total", "Total fluid: ");
    }
}
