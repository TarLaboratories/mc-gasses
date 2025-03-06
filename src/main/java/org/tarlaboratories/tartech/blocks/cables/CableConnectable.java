package org.tarlaboratories.tartech.blocks.cables;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public interface CableConnectable {
    boolean shouldConnect(BlockState state, Direction direction);
    boolean shouldAutoConnect(BlockState state, Direction direction);
    boolean isConnected(BlockState state, Direction direction);
}
