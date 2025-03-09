package org.tarlaboratories.tartech.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.blockentities.CreativeGeneratorBlockEntity;
import org.tarlaboratories.tartech.blocks.cables.CableConnectable;

public class CreativeGenerator extends BlockWithEntity implements CableConnectable {
    protected CreativeGenerator(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(CreativeGenerator::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeGeneratorBlockEntity(pos, state);
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
