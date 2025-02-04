package org.tarlaboratories.tartech;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import org.tarlaboratories.tartech.entities.GasData;

import java.util.HashMap;

public class StateSaverAndLoader extends PersistentState {
    private HashMap<ChunkPos, GasData> data;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound all_data = new NbtCompound();
        for (ChunkPos chunk : data.keySet()) {
            all_data.put(chunk.toString(), data.get(chunk).writeCustomDataToNbt(););
        }
    }
}
