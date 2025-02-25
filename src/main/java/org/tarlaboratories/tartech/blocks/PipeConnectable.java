package org.tarlaboratories.tartech.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public interface PipeConnectable {
    boolean shouldConnect(BlockState state, Direction direction);
    boolean shouldAutoConnect(BlockState state, Direction direction);
    boolean isConnected(BlockState state, Direction direction);
}
