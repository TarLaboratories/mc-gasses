package org.tarlaboratories.tartech.blocks.pipes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class FullPipeBlock extends PipeBlock {
    public FullPipeBlock(Settings settings) {
        super(settings.solid());
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
