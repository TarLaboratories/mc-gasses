package org.tarlaboratories.tartech.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Collection;

public interface Pipe {
    Collection<BlockPos> getConnectedBlocks(BlockState state, BlockView world, BlockPos pos);
    int getCapacity();
}
