package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.blockentities.ElectricalNetworkInteractor;

import java.util.ArrayList;
import java.util.List;

public class ElectricalNetwork {
    protected List<ElectricalNetworkInteractor> members = new ArrayList<>();
    protected List<Double> old_power_draw = new ArrayList<>();
    protected double generation = 0, usage = 0;

    public static @NotNull ElectricalNetwork load(@NotNull NbtCompound nbt, ServerWorld world) {
        ElectricalNetwork network = new ElectricalNetwork();
        List<BlockPos> interactors = Codec.list(BlockPos.CODEC).decode(NbtOps.INSTANCE, nbt.get("members")).getOrThrow().getFirst();
        interactors.forEach((p) -> {
            if (world.getBlockEntity(p) instanceof ElectricalNetworkInteractor i) network.addInteractor(i);
        });
        return network;
    }

    public NbtCompound toNbt() {
        NbtCompound out = new NbtCompound();
        List<BlockPos> interactors = new ArrayList<>();
        this.members.forEach((i) -> {
            if (i instanceof BlockEntity entity) interactors.add(entity.getPos());
        });
        out.put("members", Codec.list(BlockPos.CODEC).encodeStart(NbtOps.INSTANCE, interactors).getOrThrow());
        return out;
    }

    public void addInteractor(@NotNull ElectricalNetworkInteractor interactor) {
        int id = members.size();
        interactor.setElectricalNetworkGetter(() -> this);
        interactor.setModifiedCallback((i) -> {
            this.addPowerDraw(-this.old_power_draw.get(id));
            this.old_power_draw.set(id, i.getPowerDraw());
            this.addPowerDraw(i.getPowerDraw());
        });
        members.add(interactor);
        old_power_draw.add(interactor.getPowerDraw());
    }

    protected void addPowerDraw(double d) {
        if (d < 0) generation += d;
        else usage += d;
    }

    public double getSatisfaction() {
        return usage > 0 ? generation/usage : 1;
    }
}
