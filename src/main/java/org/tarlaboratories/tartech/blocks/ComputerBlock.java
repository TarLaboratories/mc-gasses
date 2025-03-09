package org.tarlaboratories.tartech.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.blockentities.ModBlockEntities;
import org.tarlaboratories.tartech.blockentities.ComputerBlockEntity;
import org.tarlaboratories.tartech.blocks.cables.CableConnectable;

public class ComputerBlock extends BlockWithEntity implements CableConnectable {
    public static final BooleanProperty IS_ON = BooleanProperty.of("is_on");

    public ComputerBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(IS_ON, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(IS_ON);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ComputerBlockEntity entity = (ComputerBlockEntity) world.getBlockEntity(pos);
        assert entity != null;
        if (stack.isEmpty()) {
            if (player.isSneaking()) {
                player.giveOrDropStack(entity.getDrive());
                entity.setDrive(ItemStack.EMPTY);
            } else {
                world.setBlockState(pos, state.with(IS_ON, !state.get(IS_ON)));
            }
            return ActionResult.SUCCESS;
        }
        ItemStack previous = entity.getDrive();
        entity.setDrive(stack);
        player.getStackInHand(hand).setCount(0);
        player.giveOrDropStack(previous);
        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.COMPUTER, ComputerBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(ComputerBlock::new);
    }

    @Override
    public boolean shouldConnect(BlockState state, Direction direction) {
        return true;
    }

    @Override
    public boolean shouldAutoConnect(BlockState state, Direction direction) {
        return true;
    }

    @Override
    public boolean isConnected(BlockState state, Direction direction) {
        return true;
    }
}
