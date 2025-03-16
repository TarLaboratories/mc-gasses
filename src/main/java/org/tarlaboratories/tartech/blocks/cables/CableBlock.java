package org.tarlaboratories.tartech.blocks.cables;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.ElectricalNetwork;
import org.tarlaboratories.tartech.StateSaverAndLoader;
import org.tarlaboratories.tartech.blockentities.CableBlockEntity;
import org.tarlaboratories.tartech.blockentities.ElectricalNetworkInteractor;

import java.util.*;

public class CableBlock extends BlockWithEntity implements Cable {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Map<Direction, BooleanProperty> CONNECTIONS = Map.of(
            Direction.UP, BooleanProperty.of("up"),
            Direction.DOWN, BooleanProperty.of("down"),
            Direction.NORTH, BooleanProperty.of("north"),
            Direction.SOUTH, BooleanProperty.of("south"),
            Direction.EAST, BooleanProperty.of("east"),
            Direction.WEST, BooleanProperty.of("west")
    );

    public static final int d = 2;

    public static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.UP, Block.createCuboidShape(8 - d, 8 + d, 8 - d, 8 + d, 16, 8 + d),
            Direction.DOWN, Block.createCuboidShape(8 - d, 0, 8 - d, 8 + d, 8 - d, 8 + d),
            Direction.NORTH, Block.createCuboidShape(8 - d, 8 - d, 0, 8 + d, 8 + d, 8 - d),
            Direction.SOUTH, Block.createCuboidShape(8 - d, 8 - d, 8 + d, 8 + d, 8 + d, 16),
            Direction.EAST, Block.createCuboidShape(8 + d, 8 - d, 8 - d, 16, 8 + d, 8 + d),
            Direction.WEST, Block.createCuboidShape(0, 8 - d, 8 - d, 8 - d, 8 + d, 8 + d)
    );

    public CableBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(CONNECTIONS.get(Direction.UP), false)
                .with(CONNECTIONS.get(Direction.DOWN), false)
                .with(CONNECTIONS.get(Direction.NORTH), false)
                .with(CONNECTIONS.get(Direction.SOUTH), false)
                .with(CONNECTIONS.get(Direction.EAST), false)
                .with(CONNECTIONS.get(Direction.WEST), false)
        );
    }

    @Override
    protected MapCodec<CableBlock> getCodec() {
        return createCodec(CableBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        for (BooleanProperty property : CONNECTIONS.values()) builder.add(property);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView ticks, BlockPos pos, Direction direction, BlockPos neighborPos, @NotNull BlockState neighborState, Random random) {
        if (neighborState.isAir()) return state.with(this.getConnectionProperty(direction), false);
        if (neighborState.getBlock() instanceof CableConnectable pipe && (pipe.isConnected(neighborState, direction.getOpposite()) || pipe.shouldAutoConnect(neighborState, direction.getOpposite()))) return state.with(this.getConnectionProperty(direction), true);
        return state;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block source, @Nullable WireOrientation wire, boolean notify) {
        if (world instanceof ServerWorld serverWorld) {
            setNewElectricalNetworkId(pos, serverWorld);
        }
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        VoxelShape out = Block.createCuboidShape(8 - d, 8 - d, 8 - d, 8 + d, 8 + d, 8 + d);
        for (Direction direction : Direction.values()) {
            if (state.get(this.getConnectionProperty(direction))) {
                out = VoxelShapes.union(out, SHAPES.get(direction));
            }
        }
        return out;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getSidesShape(state, world, pos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getSidesShape(state, world, pos);
    }

    @Override
    public BlockState getPlacementState(@NotNull ItemPlacementContext context) {
        BlockPos pos = context.getBlockPos();
        Direction side = context.getSide().getOpposite();
        BlockState out = this.getDefaultState();
        for (Direction direction : Direction.values()) {
            BlockState state = context.getWorld().getBlockState(pos.offset(direction));
            if (state.getBlock() instanceof CableConnectable connectable && connectable.shouldAutoConnect(state, direction.getOpposite())) out = out.with(this.getConnectionProperty(direction), true);
        }
        BlockState state = context.getWorld().getBlockState(pos.offset(side));
        if (state.getBlock() instanceof CableConnectable connectable && connectable.shouldConnect(state, side.getOpposite())) out = out.with(this.getConnectionProperty(side), true);
        return out;
    }

    @Override
    public Collection<BlockPos> getConnectedBlocks(BlockState state, BlockView world, BlockPos pos) {
        Collection<BlockPos> out = new ArrayList<>();
        for (Direction direction : Direction.values()) if (state.get(this.getConnectionProperty(direction))) out.add(pos.offset(direction));
        return out;
    }

    @Override
    public BooleanProperty getConnectionProperty(Direction direction) {
        return CONNECTIONS.get(direction);
    }

    @Override
    public int getCapacity() {
        return 1;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (world instanceof ServerWorld serverWorld) {
            if (this.getConnectedBlocks(state, world, pos).isEmpty()) {
                ((CableBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).setElectricalNetworkId(StateSaverAndLoader.getWorldState(serverWorld).createElectricalNetwork());
            } else setNewElectricalNetworkId(pos, serverWorld);
        }
    }

    private static void setNewElectricalNetworkId(BlockPos pos, ServerWorld world) {
        StateSaverAndLoader state = StateSaverAndLoader.getWorldState(world);
        int id = state.createElectricalNetwork();
        ElectricalNetwork network = state.getElectricalNetwork(id);
        assert network != null;
        Queue<BlockPos> q = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        q.offer(pos); visited.add(pos);
        while (!q.isEmpty()) {
            BlockPos tmp = q.poll();
            if (world.getBlockState(tmp).getBlock() instanceof Cable pipe) {
                for (BlockPos neighbour : pipe.getConnectedBlocks(world.getBlockState(tmp), world, tmp)) {
                    if (!visited.contains(neighbour) && pos.isWithinDistance(neighbour, 1000)) {
                        q.offer(neighbour);
                        visited.add(neighbour);
                    }
                }
            }
        }
        List<Integer> ids_to_remove = new ArrayList<>();
        for (BlockPos i : visited) {
            if (world.getBlockEntity(i) instanceof CableBlockEntity blockEntity && world.getBlockState(i).getBlock() instanceof Cable) {
                ids_to_remove.add(blockEntity.getElectricalNetworkId());
                blockEntity.setElectricalNetworkId(id);
            } else if (world.getBlockEntity(i) instanceof ElectricalNetworkInteractor interactor) {
                network.addInteractor(interactor);
            }
        }
        for (Integer i : ids_to_remove) state.deleteElectricalNetwork(i);
    }

    @Override
    public boolean shouldConnect(BlockState state, Direction direction) {
        return true;
    }

    @Override
    public boolean shouldAutoConnect(BlockState state, Direction direction) {
        return false;
    }

    @Override
    public boolean isConnected(@NotNull BlockState state, Direction direction) {
        return state.get(this.getConnectionProperty(direction));
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }
}
