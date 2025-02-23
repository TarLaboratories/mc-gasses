package org.tarlaboratories.tartech;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.blocks.PipeBlock;
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

    public static final RegistryKey<Block> PIPE_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "pipe"));
    public static final Block PIPE_BLOCK = register(new PipeBlock(AbstractBlock.Settings.create().registryKey(PIPE_BLOCK_KEY)), PIPE_BLOCK_KEY, true);

    public static final TagKey<Block> PIPE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "pipes"));

    public static void initialize() {}
}
