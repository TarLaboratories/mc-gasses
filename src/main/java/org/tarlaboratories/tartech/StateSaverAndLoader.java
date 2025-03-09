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
import org.tarlaboratories.tartech.blockentities.PipeBlockEntity;
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
    private static final Codec<Map<Integer, ChemicalNetwork>> CHEMICAL_NETWORK_DATA_CODEC = Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), ChemicalNetwork.CODEC);
    @SuppressWarnings("FieldMayBeFinal")
    private Map<ChunkPos, GasData> data;
    private Map<UUID, PlayerData> player_data;
    private Map<Integer, ChemicalNetwork> chemical_network_data = new HashMap<>();
    private Map<Integer, ElectricalNetwork> electrical_network_data = new HashMap<>();
    private int max_chemical_network_id, max_electrical_network_id;
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
            this.data.get(chunkPos).deleteNotNeededData();
            nbt.put(chunkPosToString.apply(chunkPos), GasData.CODEC.encodeStart(NbtOps.INSTANCE, this.data.get(chunkPos)).getOrThrow());
        }
        nbt.put("chemical_network_data", CHEMICAL_NETWORK_DATA_CODEC.encodeStart(NbtOps.INSTANCE, this.chemical_network_data).getOrThrow());
        NbtCompound tmp = new NbtCompound();
        for (Integer i : this.electrical_network_data.keySet()) tmp.put(i.toString(), this.electrical_network_data.get(i).toNbt());
        nbt.put("electrical_network_data", tmp);
        nbt.putInt("max_chemical_network_id", max_chemical_network_id);
        nbt.put("player_data", PLAYER_DATA_CODEC.encodeStart(NbtOps.INSTANCE, this.player_data).getOrThrow());
        return nbt;
    }

    public static @NotNull StateSaverAndLoader createFromNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.nbt = nbt;
        state.player_data = new HashMap<>(PLAYER_DATA_CODEC.decode(NbtOps.INSTANCE, nbt.get("player_data")).result().orElseThrow().getFirst());
        state.chemical_network_data = new HashMap<>(CHEMICAL_NETWORK_DATA_CODEC.decode(NbtOps.INSTANCE, nbt.get("chemical_network_data")).result().orElseThrow().getFirst());
        state.electrical_network_data = new HashMap<>();
        state.max_chemical_network_id = nbt.getInt("max_chemical_network_id");
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

    public @Nullable ChemicalNetwork getChemicalNetwork(@NotNull BlockPos pos) {
        if (this.world.getBlockEntity(pos) instanceof PipeBlockEntity pipeBlockEntity) {
            return this.chemical_network_data.get(pipeBlockEntity.getChemicalNetworkId());
        }
        return null;
    }

    public @Nullable ChemicalNetwork getChemicalNetwork(int id) {
        return this.chemical_network_data.get(id);
    }

    public int createChemicalNetwork() {
        this.chemical_network_data.put(max_chemical_network_id + 1, new ChemicalNetwork());
        max_chemical_network_id++;
        return max_chemical_network_id;
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

    public void deleteChemicalNetwork(int id) {
        this.chemical_network_data.remove(id);
    }

    public int createElectricalNetwork() {
        this.electrical_network_data.put(max_electrical_network_id + 1, new ElectricalNetwork());
        max_electrical_network_id++;
        return max_electrical_network_id;
    }

    public @Nullable ElectricalNetwork getElectricalNetwork(Integer id) {
        if (this.electrical_network_data.containsKey(id)) return this.electrical_network_data.get(id);
        if (this.nbt == null) return null;
        NbtCompound tmp = this.nbt.getCompound("electrical_network_data");
        if (!tmp.contains(id.toString())) return null;
        this.electrical_network_data.put(id, ElectricalNetwork.load(java.util.Objects.requireNonNull(tmp.getCompound(id.toString())), world));
        return this.electrical_network_data.get(id);
    }

    public void deleteElectricalNetwork(int id) {
        this.electrical_network_data.remove(id);
    }
}
