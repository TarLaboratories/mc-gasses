package org.tarlaboratories.tartech.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.blocks.ModBlocks;
import org.tarlaboratories.tartech.items.ModItems;
import org.tarlaboratories.tartech.chemistry.Chemical;

public abstract class ChemicalFluid extends AbstractFluid {
    @Override
    public Fluid getFlowing() {
        return ModFluids.CHEMICAL_FLUIDS.get(chemical).getRight();
    }

    @Override
    public Fluid getStill() {
        return ModFluids.CHEMICAL_FLUIDS.get(chemical).getLeft();
    }

    @Override
    public Item getBucketItem() {
        return ModItems.CHEMICAL_FLUID_BUCKETS.get(chemical);
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ModBlocks.CHEMICAL_FLUID_BLOCKS.get(chemical).getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    protected final Chemical chemical;

    public Chemical getChemical() {
        return chemical;
    }

    public ChemicalFluid(Chemical chemical) {
        this.chemical = chemical;
    }

    public static class Flowing extends ChemicalFluid {
        public Flowing(Chemical chemical) {
            super(chemical);
        }

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(@NotNull FluidState fluidState) {
            return fluidState.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends ChemicalFluid {
        public Still(Chemical chemical) {
            super(chemical);
        }

        @Override
        public int getLevel(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return true;
        }
    }
}
