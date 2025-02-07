package org.tarlaboratories.tartech;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.chemistry.Chemical;

import java.util.HashMap;
import java.util.Map;

public class StateSaverAndLoader extends PersistentState {
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<ChunkPos, GasData> data;
    private World world;
    private static final Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    private StateSaverAndLoader() {
        this.data = new HashMap<>();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound all_data = new NbtCompound();
        for (ChunkPos chunk : data.keySet()) {
            NbtElement gas_data_nbt = GasData.CODEC.encodeStart(NbtOps.INSTANCE, this.data.get(chunk)).result().orElse(new NbtCompound());
            all_data.put(chunk.toString(), gas_data_nbt);
        }
        nbt.put("gas_data", all_data);
        return nbt;
    }

    public static @NotNull StateSaverAndLoader createFromNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.data = new HashMap<>(Codec.unboundedMap(ChunkPos.CODEC, GasData.CODEC).decode(NbtOps.INSTANCE, nbt.get("gas_data")).result().orElse(Pair.of(new HashMap<>(), null)).getFirst());
        return state;
    }

    public void updateWorldAndChunkData() {
        this.data.forEach((chunkPos, gasData) -> gasData.chunk = world.getChunk(chunkPos.getStartPos()));
    }

    public void addGasDataForChunk(@NotNull Chunk chunk) {
        if (this.data.containsKey(chunk.getPos())) return;
        GasData gasData = GasData.initializeVolumesInChunk(chunk, world);
        this.data.put(chunk.getPos(), gasData);
    }

    public void reinitializeDataAtPos(@NotNull BlockPos pos) {
        Chunk chunk = world.getChunk(pos);
        GasData gasData = GasData.initializeVolumesInChunk(chunk, world);
        this.data.put(chunk.getPos(), gasData);
    }

    public void addGasAtPos(@NotNull BlockPos pos, Chemical gas, double amount) {
        if (amount > 0) this.getDataForChunk(world.getChunk(pos).getPos()).getGasVolumeAt(pos).addGas(gas, amount);
        else this.getDataForChunk(world.getChunk(pos).getPos()).getGasVolumeAt(pos).removeGas(gas, amount);
    }

    public void updateVolumesInChunk(@NotNull BlockPos pos) {
        this.addGasDataForChunk(world.getChunk(pos));
        GasData gasData = this.data.get(world.getChunk(pos).getPos());
        gasData.updateVolumesInChunk(pos);
        this.data.put(world.getChunk(pos).getPos(), gasData);
    }

    public static @NotNull StateSaverAndLoader getWorldState(@NotNull ServerWorld world) {
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, Tartech.MOD_ID);
        state.world = world;
        state.updateWorldAndChunkData();
        state.markDirty();
        return state;
    }

    public @NotNull GasData getDataForChunk(@NotNull ChunkPos chunkPos) {
        if (!this.data.containsKey(chunkPos)) this.addGasDataForChunk(this.world.getChunk(chunkPos.getStartPos()));
        return this.data.get(chunkPos);
    }
    
    public @NotNull GasVolume getGasVolumeAtPos(@NotNull BlockPos pos) {
        return this.getDataForChunk(this.world.getChunk(pos).getPos()).getGasVolumeAt(pos);
    }
}
