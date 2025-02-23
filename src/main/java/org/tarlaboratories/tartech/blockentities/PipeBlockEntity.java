package org.tarlaboratories.tartech.blockentities;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.ModBlockEntities;
import org.tarlaboratories.tartech.networking.ChemicalNetworkIdChangePayload;

public class PipeBlockEntity extends BlockEntity {
    private int chemical_network_id;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE_BLOCK_ENTITY, pos, state);
    }

    public void setChemicalNetworkId(int id) {
        if (this.chemical_network_id != id) markDirty();
        this.chemical_network_id = id;
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : PlayerLookup.tracking(serverWorld, this.getPos())) {
                ServerPlayNetworking.send(player, new ChemicalNetworkIdChangePayload(this.getPos(), id));
            }
        }
    }

    public int getChemicalNetworkId() {
        return this.chemical_network_id;
    }

    @Override
    protected void writeNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("chemical_network_id", chemical_network_id);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.setChemicalNetworkId(nbt.getInt("chemical_network_id"));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
