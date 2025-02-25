package org.tarlaboratories.tartech.blockentities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.tarlaboratories.tartech.ChemicalNetwork;
import org.tarlaboratories.tartech.ModBlockEntities;
import org.tarlaboratories.tartech.StateSaverAndLoader;
import org.tarlaboratories.tartech.blocks.Pipe;
import org.tarlaboratories.tartech.blocks.PipeOpeningBlock;
import org.tarlaboratories.tartech.gas.GasData;
import org.tarlaboratories.tartech.gas.GasVolume;

public class PipeOpeningBlockEntity extends BlockEntity {
    public PipeOpeningBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE_OPENING, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PipeOpeningBlockEntity ignoredEntity) {
        if (world instanceof ServerWorld serverWorld) {
            if (world.getBlockState(pos.offset(state.get(PipeOpeningBlock.ATTACHED_TO))).getBlock() instanceof Pipe pipe) {
                GasVolume gasVolume = GasData.get(pos, serverWorld);
                ChemicalNetwork chemicalNetwork = StateSaverAndLoader.getWorldState(serverWorld).getChemicalNetwork(pos.offset(state.get(PipeOpeningBlock.ATTACHED_TO)));
                if (chemicalNetwork == null) return;
                GasVolume result = gasVolume.getPart(pipe.getCapacity()).unexposed();
                result.mergeWith(chemicalNetwork.getPart(pipe.getCapacity()));
                result.multiplyContentsBy(0.5);
                result.addVolume(-result.getVolume()/2);
                gasVolume.multiplyContentsBy((double) (gasVolume.getVolume() - pipe.getCapacity()) / gasVolume.getVolume());
                chemicalNetwork.multiplyContentsBy((chemicalNetwork.getVolume() - pipe.getCapacity()) / chemicalNetwork.getVolume());
                gasVolume.addVolume(-pipe.getCapacity());
                chemicalNetwork.addVolume(-pipe.getCapacity());
                gasVolume.mergeWith(result);
                chemicalNetwork.mergeContentsWith(result);
            }
        }
    }
}
