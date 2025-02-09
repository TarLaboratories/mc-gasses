package org.tarlaboratories.tartech;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    private static boolean load_lock = false;
    @SuppressWarnings("FieldMayBeFinal")
    private Map<ChunkPos, GasData> data;
    private World world;
    private static final Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );
    private static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.STRING.xmap(
            (s) -> new ChunkPos(Integer.parseInt(s.split(":")[0]), Integer.parseInt(s.split(":")[1])),
            (p) -> String.format("%d:%d", p.x, p.z)
    );
    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(CHUNK_POS_CODEC, GasData.CODEC).fieldOf("data").forGetter(StateSaverAndLoader::getData)
    ).apply(instance, StateSaverAndLoader::new));

    private Map<ChunkPos, GasData> getData() {
        return this.data;
    }

    private StateSaverAndLoader() {
        this.data = new HashMap<>();
    }

    private StateSaverAndLoader(Map<ChunkPos, GasData> data) {
        this.data = data;
    }

    @Override
    public NbtCompound writeNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        DataResult<NbtElement> dataResult = CODEC.encodeStart(NbtOps.INSTANCE, this);
        if (dataResult.isError()) {
            LOGGER.error("Cannot save gas data to nbt: {}", dataResult.error().orElseThrow().message());
        } else {
            nbt.put("gas_data", dataResult.result().orElseThrow());
        }
        return nbt;
    }

    public static @NotNull StateSaverAndLoader createFromNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (load_lock) {
            LOGGER.warn("trying to load state from nbt while it is already being loaded, errors may occur");
        }
        load_lock = true;
        DataResult<StateSaverAndLoader> dataResult = CODEC.parse(NbtOps.INSTANCE, nbt.get("gas_data"));
        load_lock = false;
        if (dataResult.isError()) {
            LOGGER.error("Cannot load gas data from nbt: {}", dataResult.error().orElseThrow().message());
            return new StateSaverAndLoader();
        } else {
            return dataResult.getOrThrow();
        }
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
        gasData.updateVolumesInChunk();
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
