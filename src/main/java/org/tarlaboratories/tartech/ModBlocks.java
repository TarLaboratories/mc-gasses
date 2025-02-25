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
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.blocks.FullPipeBlock;
import org.tarlaboratories.tartech.blocks.PipeBlock;
import org.tarlaboratories.tartech.blocks.PipeOpeningBlock;
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

    public static final RegistryKey<Block> PIPE_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "pipe"));
    public static final Block PIPE = register(new PipeBlock(AbstractBlock.Settings.create().registryKey(PIPE_KEY)), PIPE_KEY, true);

    public static final RegistryKey<Block> FULL_PIPE_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "full_pipe"));
    public static final Block FULL_PIPE = register(new FullPipeBlock(AbstractBlock.Settings.create().registryKey(FULL_PIPE_KEY)), FULL_PIPE_KEY, true);

    public static final RegistryKey<Block> PIPE_OPENING_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "pipe_opening"));
    public static final Block PIPE_OPENING = register(new PipeOpeningBlock(AbstractBlock.Settings.create().registryKey(PIPE_OPENING_KEY)), PIPE_OPENING_KEY, true);

    public static void initialize() {}
}
