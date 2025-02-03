package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.items.GasAnalyzerItem;

public class ModItems {
    public static <T extends Item> T register(T item, RegistryKey<Item> registryKey) {
        return Registry.register(Registries.ITEM, registryKey, item);
    }

    public static final RegistryKey<Item> TEST_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "test_item"));
    public static final Item TEST_ITEM = register(new Item(new Item.Settings().registryKey(TEST_ITEM_KEY)), TEST_ITEM_KEY);
    public static final RegistryKey<Item> GAS_ANALYSER_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tartech.MOD_ID, "gas_analyser"));
    public static final GasAnalyzerItem GAS_ANALYZER_ITEM = register(new GasAnalyzerItem(new Item.Settings().registryKey(GAS_ANALYSER_KEY)), GAS_ANALYSER_KEY);

    public static final RegistryKey<ItemGroup> MAIN_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(Tartech.MOD_ID, "main_item_group"));
    public static final ItemGroup MAIN_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(TEST_ITEM))
            .displayName(Text.translatable("main_item_group.tartech"))
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, MAIN_ITEM_GROUP_KEY, MAIN_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(MAIN_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(TEST_ITEM);
            itemGroup.add(GAS_ANALYZER_ITEM);
        });
    }
}
