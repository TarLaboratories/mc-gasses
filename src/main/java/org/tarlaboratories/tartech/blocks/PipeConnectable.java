package org.tarlaboratories.tartech.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public interface PipeConnectable {
    boolean shouldConnect(BlockState state, Direction direction);
}
