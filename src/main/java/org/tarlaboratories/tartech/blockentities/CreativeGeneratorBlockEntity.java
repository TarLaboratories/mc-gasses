package org.tarlaboratories.tartech.blockentities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.tarlaboratories.tartech.ElectricalNetwork;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreativeGeneratorBlockEntity extends BlockEntity implements ElectricalNetworkInteractor {
    public CreativeGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_GENERATOR, pos, state);
    }

    @Override
    public double getPowerDraw() {
        return -100;
    }

    @Override
    public void setModifiedCallback(Consumer<ElectricalNetworkInteractor> callback) {

    }

    @Override
    public void setElectricalNetworkGetter(Supplier<ElectricalNetwork> getter) {

    }
}
