package org.tarlaboratories.tartech;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.gas.GasData;
import org.tarlaboratories.tartech.gas.GasVolume;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class StateSaverAndLoader extends PersistentState {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Codec<Map<UUID, PlayerData>> PLAYER_DATA_CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, PlayerData.CODEC);
    @SuppressWarnings("FieldMayBeFinal")
    private Map<ChunkPos, GasData> data;
    private Map<UUID, PlayerData> player_data;
    private ServerWorld world;
    private @Nullable NbtCompound nbt = null;
    private static final Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    private static final Function<ChunkPos, String> chunkPosToString = (p) -> String.format("%d:%d", p.x, p.z);

    private StateSaverAndLoader() {
        this.data = new HashMap<>();
        this.player_data = new HashMap<>();
    }

    @Override
    public NbtCompound writeNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        for (ChunkPos chunkPos : this.data.keySet()) {
            this.data.get(chunkPos).deleteNotNeededData(world);
            nbt.put(chunkPosToString.apply(chunkPos), GasData.CODEC.encodeStart(NbtOps.INSTANCE, this.data.get(chunkPos)).getOrThrow());
        }
        nbt.put("player_data", PLAYER_DATA_CODEC.encodeStart(NbtOps.INSTANCE, this.player_data).getOrThrow());
        return nbt;
    }

    public static @NotNull StateSaverAndLoader createFromNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.nbt = nbt;
        state.player_data = new HashMap<>(PLAYER_DATA_CODEC.decode(NbtOps.INSTANCE, nbt.get("player_data")).result().orElseThrow().getFirst());
        return state;
    }

    public void updateWorldAndChunkData() {
        this.data.forEach((chunkPos, gasData) -> gasData.chunk = world.getChunk(chunkPos.getStartPos()));
    }

    public void addGasDataForChunk(@NotNull Chunk chunk) {
        if (this.data.containsKey(chunk.getPos())) return;
        if (this.nbt != null && this.nbt.contains(chunkPosToString.apply(chunk.getPos()))) {
            GasData gasData = GasData.CODEC.parse(NbtOps.INSTANCE, this.nbt.get(chunkPosToString.apply(chunk.getPos()))).getOrThrow();
            gasData.chunk = chunk;
            this.data.put(chunk.getPos(), gasData);
        } else {
            GasData gasData = GasData.initializeVolumesInChunk(chunk, world);
            this.data.put(chunk.getPos(), gasData);
        }
    }

    public void reinitializeDataAtPos(@NotNull BlockPos pos) {
        Chunk chunk = world.getChunk(pos);
        GasData gasData = GasData.initializeVolumesInChunk(chunk, world);
        this.data.put(chunk.getPos(), gasData);
    }

    public void addGasAtPos(@NotNull BlockPos pos, Chemical gas, double amount) {
        if (amount > 0) this.getDataForChunk(world.getChunk(pos).getPos()).getGasVolumeAt(pos).addGas(gas, amount);
        else this.getDataForChunk(world.getChunk(pos).getPos()).getGasVolumeAt(pos).removeGas(gas, -amount);
    }

    public void updateVolumesInChunk(@NotNull BlockPos pos) {
        this.addGasDataForChunk(world.getChunk(pos));
        GasData gasData = this.data.get(world.getChunk(pos).getPos());
        gasData.updateVolumesInChunk(world);
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

    public static @NotNull PlayerData getPlayerData(@NotNull LivingEntity player) {
        if (player.getServer() == null) return new PlayerData();
        StateSaverAndLoader state = StateSaverAndLoader.getWorldState(player.getServer().getOverworld());
        return state.player_data.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StateSaverAndLoader that = (StateSaverAndLoader) o;
        return Objects.equal(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }
}
