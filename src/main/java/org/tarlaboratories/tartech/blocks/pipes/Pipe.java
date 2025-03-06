package org.tarlaboratories.tartech.blocks.pipes;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.Collection;

public interface Pipe extends PipeConnectable {
    Collection<BlockPos> getConnectedBlocks(BlockState state, BlockView world, BlockPos pos);
    BooleanProperty getConnectionProperty(Direction direction);
    int getCapacity();
}
