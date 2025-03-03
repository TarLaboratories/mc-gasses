package org.tarlaboratories.tartech;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.blockentities.ComputerBlockEntity;
import org.tarlaboratories.tartech.blockentities.PipeBlockEntity;
import org.tarlaboratories.tartech.blockentities.PipeOpeningBlockEntity;

public class ModBlockEntities {
    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
                                                                       Block... blocks) {
        Identifier id = Identifier.of(Tartech.MOD_ID, name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static final BlockEntityType<PipeBlockEntity> PIPE = register("pipe", PipeBlockEntity::new, ModBlocks.PIPE, ModBlocks.FULL_PIPE);
    public static final BlockEntityType<PipeOpeningBlockEntity> PIPE_OPENING = register("pipe_opening", PipeOpeningBlockEntity::new, ModBlocks.PIPE_OPENING);
    public static final BlockEntityType<ComputerBlockEntity> COMPUTER = register("computer", ComputerBlockEntity::new, ModBlocks.COMPUTER_BLOCK);
}
