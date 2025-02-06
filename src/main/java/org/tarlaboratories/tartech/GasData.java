package org.tarlaboratories.tartech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.chemistry.ChemicalElement;

import java.util.*;
import java.util.function.Predicate;

public class GasData {
    public static HashSet<ChunkPos> currentlyInitializing = new HashSet<>();
    protected List<List<List<Integer>>> data;
    protected Map<Integer, GasVolume> gas_data;
    protected Integer max_volume_id = 0;
    private boolean initialized_data = false;
    public static final Codec<GasData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.list(Codec.list(Codec.INT))).fieldOf("data").forGetter(GasData::getData),
            Codec.unboundedMap(Codec.INT, GasVolume.CODEC).fieldOf("gas_data").forGetter(GasData::getGasData),
            Codec.INT.fieldOf("max_volume_id").forGetter(GasData::getMaxVolumeId),
            Codec.BOOL.fieldOf("initialized_data").forGetter(GasData::isInitializedData)
    ).apply(instance, GasData::new));
    protected Chunk chunk;
    protected ChunkPos chunkPos;
    protected RegistryKey<DimensionType> dimension;
    protected final HashMap<RegistryKey<DimensionType>, GasVolume> DEFAULT_GAS_VOLUMES = new HashMap<>(Map.of(
            DimensionTypes.OVERWORLD, (new GasVolume()).addGas(Chemical.OXYGEN, 0.2).addGas(Chemical.primitiveOf(ChemicalElement.NITROGEN.getElement()), 0.8).setTemperature(20),
            DimensionTypes.THE_NETHER, (new GasVolume()).addGas(Chemical.SULFUR_DIOXIDE, 0.5).setTemperature(80),
            DimensionTypes.THE_END, (new GasVolume()).setTemperature(-50)
    ));

    private static final Logger LOGGER = LogManager.getLogger();

    public GasData(@NotNull World world, @NotNull Chunk chunk) {
        this.chunk = chunk;
        this.chunkPos = chunk.getPos();
        this.dimension = world.getDimensionEntry().getKey().orElse(DimensionTypes.OVERWORLD);
    }

    protected GasData(List<List<List<Integer>>> data, Map<Integer, GasVolume> gas_data, int max_volume_id, boolean initialized_data) {
        this.data = data;
        this.gas_data = gas_data;
        this.max_volume_id = max_volume_id;
        this.initialized_data = initialized_data;
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public List<List<List<Integer>>> getData() {
        return this.data;
    }

    public Map<Integer, GasVolume> getGasData() {
        return this.gas_data;
    }

    public Integer getMaxVolumeId() {
        return this.max_volume_id;
    }

    public boolean isInitializedData() {
        return initialized_data;
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        if (!this.initialized_data) this.initializeData();
        for (int i = 0; i < this.data.size(); i++) {
            NbtCompound yz = new NbtCompound();
            for (int j = 0; j < this.data.get(i).size(); j++) {
                NbtCompound z = new NbtCompound();
                for (int k = 0; k < this.data.get(i).get(j).size(); k++) z.putInt(Integer.toString(k), this.data.get(i).get(j).get(k));
                yz.put(Integer.toString(j), z);
            }
            nbt.put(Integer.toString(i), yz);
        }
        nbt.put("gas_data", Codec.unboundedMap(Codec.INT, GasVolume.CODEC).encodeStart(NbtOps.INSTANCE, gas_data).result().orElse(new NbtCompound()));
    }

    protected void initializeData() {
        this.data = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            this.data.add(new ArrayList<>());
            for (int j = 0; j <= chunk.getHeight(); j++) {
                this.data.get(i).add(new ArrayList<>());
                for (int k = 0; k < 16; k++) this.data.get(i).get(j).add(-1);
            }
        }
        this.gas_data = new HashMap<>();
        this.initialized_data = true;
    }

    protected List<BlockPos> getNeighboursWithSameGas(@NotNull BlockPos pos) {
        List<BlockPos> out = new ArrayList<>();
        BlockState state = this.chunk.getBlockState(pos);
        for (Direction direction : List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH)) {
            if (this.isAboveHeightLimit(pos.offset(direction))) continue;
            if (this.isInSameChunk(pos.offset(direction)) && !state.isSideSolidFullSquare(this.chunk, pos, direction) &&
                    !this.chunk.getBlockState(pos.offset(direction)).isSideSolidFullSquare(this.chunk, pos.offset(direction), direction.getOpposite())) out.add(pos.offset(direction));
        }
        return out;
    }

    protected boolean isInSameChunk(@NotNull BlockPos pos) {
        return this.getChunkPos().getStartX() <= pos.getX() && this.getChunkPos().getStartZ() <= pos.getZ() &&
                this.getChunkPos().getEndX() >= pos.getX() && this.getChunkPos().getEndZ() >= pos.getZ();
    }

    protected boolean isAboveHeightLimit(@NotNull BlockPos pos) {
        return this.chunk.getBottomY() + chunk.getHeight() <= (pos.getY() + 100);
    }

    protected int getVolumeIdAt(@NotNull BlockPos pos) {
        if (!this.initialized_data) initializeData();
        return this.data.get(pos.getX() - this.getChunkPos().getStartX()).get(Math.min(Math.max(pos.getY() - this.chunk.getBottomY(), 0), 255)).get(pos.getZ() - this.getChunkPos().getStartZ());
    }

    protected @NotNull GasVolume getGasVolumeAt(@NotNull BlockPos pos) {
        int tmp = this.getVolumeIdAt(pos);
        if (tmp == -1 && this.canContainGas(pos)) {
            LOGGER.warn("volume id is -1, but block can contain gas at pos = {}", pos);
            return (new GasVolume()).addVolume(1);
        } else if (tmp == -1) return new GasVolume();
        return this.gas_data.getOrDefault(tmp, new GasVolume());
    }

    public static GasVolume getGasVolumeAt(@NotNull BlockPos pos, @NotNull ServerWorld world) {
        GasData tmp = getEntityForChunk(world.getChunk(pos), world);
        return tmp.getGasVolumeAt(pos);
    }

    protected void setVolumeIdAt(@NotNull BlockPos pos, int id) {
        if (!this.initialized_data) initializeData();
        this.data.get(pos.getX() - this.getChunkPos().getStartX()).get(pos.getY() - this.chunk.getBottomY()).set(pos.getZ() - this.getChunkPos().getStartZ(), id);
    }

    protected Set<BlockPos> setVolumeAtPos(@NotNull BlockPos pos, int id) {
        Set<BlockPos> tmp = this.getConnectedBlocks(pos);
        for (BlockPos pos1 : tmp) this.setVolumeIdAt(pos1, id);
        return tmp;
    }

    protected boolean canContainGas(@NotNull BlockPos pos) {
        assert !this.chunk.isOutOfHeightLimit(pos);
        return !this.getNeighboursWithSameGas(pos).isEmpty() || this.chunk.getBlockState(pos).isAir();
    }

    protected Set<BlockPos> getConnectedBlocks(@NotNull BlockPos pos, @NotNull Predicate<BlockPos> predicate, @Range(from = 1, to = 1000) int max_dist) {
        Queue<BlockPos> q = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        if (!this.isInSameChunk(pos) || this.isAboveHeightLimit(pos) || !this.canContainGas(pos)) return visited;
        q.offer(pos); visited.add(pos);
        while (!q.isEmpty()) {
            BlockPos tmp = q.poll();
            for (BlockPos neighbour : this.getNeighboursWithSameGas(tmp)) {
                assert this.chunk.getBlockState(neighbour).isTransparent();
                if (!visited.contains(neighbour) && pos.isWithinDistance(neighbour, max_dist)) {
                    q.offer(neighbour);
                    visited.add(neighbour);
                    if (predicate.test(neighbour)) return visited;
                }
            }
        }
        return visited;
    }

    protected Set<BlockPos> getConnectedBlocks(@NotNull BlockPos pos) {
        return this.getConnectedBlocks(pos, (p) -> false, 1000);
    }
    protected void updateVolumeAtPos(@NotNull BlockPos pos) {
        int old_volume_id = this.getVolumeIdAt(pos);
        Set<BlockPos> connected_blocks = this.getConnectedBlocks(pos); //, (p) -> false, 6);
        GasVolume gasVolume = new GasVolume();
        for (BlockPos tmp_pos : connected_blocks) {
            gasVolume.mergeWith(this.getGasVolumeAt(tmp_pos).getPart(1));
            this.setVolumeIdAt(tmp_pos, max_volume_id + 1);
        }
        LOGGER.info("idk, pressure is {}", gasVolume.getPressure());
        this.gas_data.put(max_volume_id + 1, gasVolume);
        max_volume_id++;
        /*
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < this.chunk.getHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    if (!connected_blocks.contains(chunkPos.getBlockPos(x, y - chunk.getBottomY(), z)) && this.data.get(x).get(y).get(z) == old_volume_id) {
                        this.data.get(x).get(y).set(z, max_volume_id + 1);
                        max_volume_id++;
                        this.updateVolumeAtPos(chunkPos.getBlockPos(x, y - chunk.getBottomY(), z));
                    }
                }
            }
        }*/
    }

    public static void updateVolumeAtPos(@NotNull BlockPos pos, @NotNull ServerWorld world) {
        GasData tmp = getEntityForChunk(world.getChunk(pos), world);
        tmp.updateVolumeAtPos(pos);
    }

    public GasVolume getDefaultGasVolume() {
        if (DEFAULT_GAS_VOLUMES.containsKey(this.dimension)) {
            return DEFAULT_GAS_VOLUMES.get(this.dimension).copy();
        } else {
            return DEFAULT_GAS_VOLUMES.get(DimensionTypes.OVERWORLD).copy();
        }
    }

    protected void updateVolumesInChunk() {
        this.initializeData();
        HashSet<BlockPos> tmp = new HashSet<>();
        int cur_volume_id = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = this.chunk.getBottomY(); y <= this.chunk.getHeight() + this.chunk.getBottomY(); y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos tmp_pos = this.getChunkPos().getBlockPos(x, y, z);
                    if (!tmp.contains(tmp_pos)) {
                        HashSet<BlockPos> updated_blocks = new HashSet<>(this.setVolumeAtPos(tmp_pos, cur_volume_id));
                        this.gas_data.put(this.gas_data.size(), this.getDefaultGasVolume().addVolume(updated_blocks.size()).multiplyContentsBy(updated_blocks.size()));
                        tmp.addAll(updated_blocks);
                        cur_volume_id++;
                    }
                }
            }
        }
        this.max_volume_id = this.gas_data.size();
    }

    protected static @NotNull GasData getEntityForChunk(@NotNull Chunk chunk, ServerWorld world) {
        return StateSaverAndLoader.getWorldState(world).getDataForChunk(chunk.getPos());
    }

    public static @NotNull GasData initializeVolumesInChunk(@NotNull Chunk chunk, World world) {
        if (GasData.currentlyInitializing.contains(chunk.getPos())) {
            LOGGER.warn("Attempt to initialize volumes in chunk {} in world {} while they are already being initialized", chunk.getPos(), world);
        }
        GasData.currentlyInitializing.add(chunk.getPos());
        GasData data = new GasData(world, chunk);
        data.updateVolumesInChunk();
        LOGGER.info("initializing gas data for chunk {}", chunk.getPos());
        GasData.currentlyInitializing.remove(chunk.getPos());
        return data;
    }
}
