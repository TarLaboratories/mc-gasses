package org.tarlaboratories.tartech;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.Map;

@SuppressWarnings("unused")
public class ModBlocks {
    public static Block register(Block block, RegistryKey<Block> blockKey, boolean shouldRegisterItem) {
        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }
        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    public static final Map<Chemical, RegistryKey<Block>> CHEMICAL_FLUID_BLOCK_KEYS = Chemical.forEachChemical(((chemical, properties) -> RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("chemical_fluid_" + chemical.toIdentifierString()))));
    public static final Map<Chemical, Block> CHEMICAL_FLUID_BLOCKS = Chemical.forEachChemical(((chemical, properties) -> register(new FluidBlock(ModFluids.CHEMICAL_FLUIDS.get(chemical).getLeft(), Blocks.WATER.getSettings()), CHEMICAL_FLUID_BLOCK_KEYS.get(chemical), false)));

    public static void initialize() {}
}
