package org.tarlaboratories.tartech.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.ModBlocks;

public class PipeOpeningBlock extends Block {
    public static final EnumProperty<Direction> ATTACHED_TO = EnumProperty.of("attached_to", Direction.class);

    public PipeOpeningBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPlacementState(@NotNull ItemPlacementContext context) {
        return getDefaultState().with(ATTACHED_TO, context.getSide());
    }

    @Override
    public void onPlaced(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, ItemStack itemStack) {
        BlockState state2 = world.getBlockState(pos.offset(state.get(ATTACHED_TO)));
        if (state2.isIn(ModBlocks.PIPE_TAG)) {
            world.setBlockState(pos.offset(state.get(ATTACHED_TO)), state2.withIfExists(PipeBlock.CONNECTIONS.get(state.get(ATTACHED_TO).getOpposite()), true));
        }
    }

    @Override
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
        builder.add(ATTACHED_TO);
    }
}
