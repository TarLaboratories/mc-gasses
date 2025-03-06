package org.tarlaboratories.tartech.blocks.cables;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class FullCableBlock extends CableBlock {
    public FullCableBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public boolean shouldAutoConnect(BlockState state, Direction direction) {
        return true;
    }
}
