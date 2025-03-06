package org.tarlaboratories.tartech.blocks;

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
import org.tarlaboratories.tartech.blocks.cables.CableBlock;
import org.tarlaboratories.tartech.blocks.cables.FullCableBlock;
import org.tarlaboratories.tartech.blocks.pipes.PipeOpeningBlock;
import org.tarlaboratories.tartech.fluids.ModFluids;
import org.tarlaboratories.tartech.Tartech;
import org.tarlaboratories.tartech.blocks.pipes.FullPipeBlock;
import org.tarlaboratories.tartech.blocks.pipes.PipeBlock;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.Map;

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

    public static final RegistryKey<Block> COMPUTER_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "computer"));
    public static final Block COMPUTER_BLOCK = register(new ComputerBlock(AbstractBlock.Settings.create().registryKey(COMPUTER_BLOCK_KEY)), COMPUTER_BLOCK_KEY, true);

    public static final RegistryKey<Block> CABLE_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "cable"));
    public static final Block CABLE = register(new CableBlock(AbstractBlock.Settings.create().registryKey(CABLE_KEY)), CABLE_KEY, true);

    public static final RegistryKey<Block> FULL_CABLE_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Tartech.MOD_ID, "full_cable"));
    public static final Block FULL_CABLE = register(new FullCableBlock(AbstractBlock.Settings.create().registryKey(FULL_CABLE_KEY)), FULL_CABLE_KEY, true);

    public static void initialize() {}
}
