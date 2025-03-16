package org.tarlaboratories.tartech.items;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.fluids.ModFluids;
import org.tarlaboratories.tartech.Tartech;
import org.tarlaboratories.tartech.blocks.ModBlocks;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.Map;

public class ModItems {
    public static <T extends Item> T register(T item, RegistryKey<Item> registryKey) {
        return Registry.register(Registries.ITEM, registryKey, item);
    }

    public static final RegistryKey<Item> TEST_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "test_item"));
    public static final Item TEST_ITEM = register(new Item(new Item.Settings().registryKey(TEST_ITEM_KEY)), TEST_ITEM_KEY);

    public static final RegistryKey<Item> GAS_TEST_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "gas_test_item"));
    public static final Item GAS_TEST_ITEM = register(new Item(new Item.Settings().registryKey(GAS_TEST_ITEM_KEY)), GAS_TEST_ITEM_KEY);

    public static final RegistryKey<Item> GAS_ANALYSER_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "gas_analyser"));
    public static final GasAnalyserItem GAS_ANALYSER_ITEM = register(new GasAnalyserItem(new Item.Settings().registryKey(GAS_ANALYSER_KEY)), GAS_ANALYSER_KEY);

    public static final Map<Chemical, RegistryKey<Item>> CHEMICAL_FLUID_BUCKET_KEYS = Chemical.forEachChemical((chemical, properties) -> RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "chemical_fluid_" + chemical.toIdentifierString() + "_bucket")));
    public static final Map<Chemical, Item> CHEMICAL_FLUID_BUCKETS = Chemical.forEachChemical((chemical, properties) -> register(new BucketItem(ModFluids.CHEMICAL_FLUIDS.get(chemical).getLeft(), new Item.Settings().registryKey(CHEMICAL_FLUID_BUCKET_KEYS.get(chemical))), CHEMICAL_FLUID_BUCKET_KEYS.get(chemical)));

    public static final RegistryKey<Item> FLUID_STORAGE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "fluid_storage"));
    public static final FluidStorageItem FLUID_STORAGE = register(new FluidStorageItem(new Item.Settings().registryKey(FLUID_STORAGE_KEY)), FLUID_STORAGE_KEY);

    public static final RegistryKey<Item> RENDERING_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "rendering_item"));
    public static final RenderingItem RENDERING_ITEM = register(new RenderingItem(new Item.Settings().registryKey(RENDERING_ITEM_KEY)), RENDERING_ITEM_KEY);

    public static final TagKey<Item> CHEMICAL_FLUID_BUCKET_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "chemical_fluid_buckets"));

    public static final TagKey<Item> DEBUG_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "debug"));

    public static final RegistryKey<ItemGroup> MAIN_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(Tartech.MOD_ID, "main_item_group"));
    public static final ItemGroup MAIN_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(GAS_TEST_ITEM))
            .displayName(Text.translatable("tartech.main_item_group"))
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, MAIN_ITEM_GROUP_KEY, MAIN_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(MAIN_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(TEST_ITEM);
            itemGroup.add(GAS_ANALYSER_ITEM);
            itemGroup.add(GAS_TEST_ITEM);
            itemGroup.add(FLUID_STORAGE);
            Chemical.forEachChemical((c, p) -> {
                itemGroup.add(FLUID_STORAGE.getWithFluid(ModFluids.CHEMICAL_FLUIDS.get(c).getLeft(), 4));
                return null;
            });
            itemGroup.add(ModBlocks.PIPE.asItem());
            itemGroup.add(ModBlocks.FULL_PIPE.asItem());
            itemGroup.add(ModBlocks.PIPE_OPENING.asItem());
            itemGroup.add(ModBlocks.CABLE.asItem());
            itemGroup.add(ModBlocks.FULL_CABLE.asItem());
        });
    }
}
