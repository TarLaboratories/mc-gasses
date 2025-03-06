package org.tarlaboratories.tartech.blocks.pipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.blockentities.ModBlockEntities;
import org.tarlaboratories.tartech.blockentities.PipeOpeningBlockEntity;

import java.util.Map;

public class PipeOpeningBlock extends BlockWithEntity implements PipeConnectable {
    private static final int d = 1, w = 8;
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.UP, Block.createCuboidShape(8 - w, 16 - d, 8 - w, 8 + w, 16, 8 + w),
            Direction.DOWN, Block.createCuboidShape(8 - w, 0, 8 - w, 8 + w, d, 8 + w),
            Direction.NORTH, Block.createCuboidShape(8 - w, 8 - w, 0, 8 + w, 8 + w, d),
            Direction.SOUTH, Block.createCuboidShape(8 - w, 8 - w, 16 - d, 8 + w, 8 + w, 16),
            Direction.EAST, Block.createCuboidShape(16 - d, 8 - w, 8 - w, 16, 8 + w, 8 + w),
            Direction.WEST, Block.createCuboidShape(0, 8 - w, 8 - w, d, 8 + w, 8 + w)
    );
    public static final EnumProperty<Direction> ATTACHED_TO = EnumProperty.of("attached_to", Direction.class);

    public PipeOpeningBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<PipeOpeningBlock> getCodec() {
        return createCodec(PipeOpeningBlock::new);
    }

    @Override
    public BlockState getPlacementState(@NotNull ItemPlacementContext context) {
        return getDefaultState().with(ATTACHED_TO, context.getSide().getOpposite());
    }

    @Override
    public void onPlaced(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, ItemStack itemStack) {
        BlockState state2 = world.getBlockState(pos.offset(state.get(ATTACHED_TO)));
        if (state2.getBlock() instanceof Pipe pipe) {
            world.setBlockState(pos.offset(state.get(ATTACHED_TO)), state2.withIfExists(pipe.getConnectionProperty(state.get(ATTACHED_TO).getOpposite()), true));
        }
    }

    @Override
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
        builder.add(ATTACHED_TO);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PipeOpeningBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.PIPE_OPENING, PipeOpeningBlockEntity::tick);
    }

    @Override
    protected VoxelShape getSidesShape(@NotNull BlockState state, BlockView world, BlockPos pos) {
        return SHAPES.get(state.get(ATTACHED_TO));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getSidesShape(state, world, pos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getSidesShape(state, world, pos);
    }

    @Override
    public boolean shouldConnect(@NotNull BlockState state, Direction direction) {
        return state.get(ATTACHED_TO).equals(direction);
    }

    @Override
    public boolean shouldAutoConnect(BlockState state, Direction direction) {
        return shouldConnect(state, direction);
    }

    @Override
    public boolean isConnected(BlockState state, Direction direction) {
        return shouldConnect(state, direction);
    }
}
