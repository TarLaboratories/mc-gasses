package org.tarlaboratories.tartech;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.chemistry.ChemicalElement;

import java.util.*;
import java.util.function.Predicate;

public class GasData {
    public static HashSet<ChunkPos> currentlyInitializing = new HashSet<>();
    protected List<List<List<Integer>>> data;
    protected Map<Integer, GasVolume> gas_data;
    protected Integer max_volume_id = 0, old_max_volume_id = -2;
    private boolean initialized_data = false;
    private static final Codec<Integer> INTEGER_CODEC = Codec.STRING.xmap(Integer::parseInt, Object::toString);
    public static final Codec<GasData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.list(Codec.list(Codec.INT))).fieldOf("data").forGetter(GasData::getData),
            Codec.unboundedMap(INTEGER_CODEC, GasVolume.CODEC).fieldOf("gas_data").forGetter(GasData::getGasData),
            Codec.INT.fieldOf("max_volume_id").forGetter(GasData::getMaxVolumeId),
            Codec.INT.fieldOf("old_max_volume_id").forGetter(GasData::getOldMaxVolumeId),
            Codec.BOOL.fieldOf("initialized_data").forGetter(GasData::isInitializedData),
            ChunkPos.CODEC.fieldOf("chunk_pos").forGetter(GasData::getChunkPos)
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

    public GasData(@NotNull WorldView world, @NotNull Chunk chunk) {
        this.chunk = chunk;
        this.chunkPos = chunk.getPos();
        this.dimension = world.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE).getKey(world.getDimension()).orElseThrow();
    }

    protected GasData(List<List<List<Integer>>> data, Map<Integer, GasVolume> gas_data, int max_volume_id, int old_max_volume_id, boolean initialized_data, ChunkPos pos) {
        this.data = data;
        this.gas_data = gas_data;
        this.max_volume_id = max_volume_id;
        this.old_max_volume_id = old_max_volume_id;
        this.initialized_data = initialized_data;
        this.chunkPos = pos;
    }

    public void deleteNotNeededData() {
        for (Integer i : this.gas_data.keySet()) {
            if (i <= this.old_max_volume_id) this.gas_data.remove(i);
        }
    }

    protected Integer getOldMaxVolumeId() {
        return this.old_max_volume_id;
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

    protected void initializeData() {
        this.data = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            this.data.add(new ArrayList<>());
            for (int j = 0; j <= chunk.getHeight(); j++) {
                this.data.get(i).add(new ArrayList<>());
                for (int k = 0; k < 16; k++) this.data.get(i).get(j).add(-2);
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
        return this.chunk.getBottomY() + chunk.getHeight() <= pos.getY();
    }

    protected int getVolumeIdAt(@NotNull BlockPos pos) {
        if (!this.initialized_data) initializeData();
        return this.data.get(pos.getX() - this.getChunkPos().getStartX()).get(pos.getY() - this.chunk.getBottomY()).get(pos.getZ() - this.getChunkPos().getStartZ());
    }

    protected @NotNull GasVolume getGasVolumeAt(@NotNull BlockPos pos) {
        int tmp = this.getVolumeIdAt(pos);
        if (tmp == -1 && this.canContainGas(pos)) {
            LOGGER.warn("volume id is -1, but block can contain gas at pos = {}", pos);
            return (new GasVolume()).addVolume(1);
        } else if (tmp == -1) return new GasVolume();
        else if (tmp <= old_max_volume_id) return this.getDefaultGasVolume().multiplyContentsBy(10000).addVolume(10000);
        return this.gas_data.getOrDefault(tmp, new GasVolume());
    }

    public static @NotNull GasVolume getGasVolumeAt(@NotNull BlockPos pos, @NotNull ServerWorld world) {
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

    protected Set<BlockPos> getConnectedBlocks(@NotNull BlockPos pos, @NotNull Predicate<BlockPos> predicate) {
        Queue<BlockPos> q = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        if (!this.isInSameChunk(pos) || this.isAboveHeightLimit(pos) || !this.canContainGas(pos)) return visited;
        q.offer(pos); visited.add(pos);
        while (!q.isEmpty()) {
            BlockPos tmp = q.poll();
            for (BlockPos neighbour : this.getNeighboursWithSameGas(tmp)) {
                assert this.chunk.getBlockState(neighbour).isTransparent();
                if (!visited.contains(neighbour) && pos.isWithinDistance(neighbour, 1000)) {
                    q.offer(neighbour);
                    visited.add(neighbour);
                    if (predicate.test(neighbour)) return visited;
                }
            }
        }
        return visited;
    }

    protected Set<BlockPos> getConnectedBlocks(@NotNull BlockPos pos) {
        return this.getConnectedBlocks(pos, (p) -> false);
    }

    protected int getHighestY(int x, int z) {
        return this.chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(x, z);
    }

    protected boolean canSeeSky(@NotNull BlockPos pos) {
        return this.getHighestY(pos.getX() - this.chunkPos.getStartX(), pos.getZ() - this.chunkPos.getStartZ()) <= pos.getY();
    }

    protected Set<BlockPos> updateVolumeAtPos(@NotNull BlockPos pos) {
        Set<BlockPos> connected_blocks = this.getConnectedBlocks(pos, this::canSeeSky);
        if (connected_blocks.stream().anyMatch(this::canSeeSky)) {
            return connected_blocks;
        }
        GasVolume gasVolume = new GasVolume();
        gasVolume.setTemperature(this.getDefaultGasVolume().getTemperature());
        gasVolume.setRadioactivity(this.getDefaultGasVolume().getRadioactivity());
        for (BlockPos tmp_pos : connected_blocks) {
            gasVolume.mergeWith(this.getGasVolumeAt(tmp_pos).getPart(1));
            this.setVolumeIdAt(tmp_pos, max_volume_id + 1);
        }
        this.gas_data.put(max_volume_id + 1, gasVolume);
        max_volume_id++;
        return connected_blocks;
    }

    protected void updateVolumesInChunk() {
        this.old_max_volume_id = this.max_volume_id;
        Set<BlockPos> tmp = new HashSet<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = this.chunk.getBottomY(); y <= this.getHighestY(x, z); y++) {
                    BlockPos tmp_pos = this.chunkPos.getBlockPos(x, y, z);
                    if (!this.canContainGas(tmp_pos)) this.setVolumeIdAt(tmp_pos, -1);
                    else if (!tmp.contains(tmp_pos)) tmp.addAll(this.updateVolumeAtPos(tmp_pos));
                }
            }
        }
    }

    public GasVolume getDefaultGasVolume() {
        if (DEFAULT_GAS_VOLUMES.containsKey(this.dimension)) {
            return DEFAULT_GAS_VOLUMES.get(this.dimension).copy();
        } else {
            return DEFAULT_GAS_VOLUMES.get(DimensionTypes.OVERWORLD).copy();
        }
    }

    protected void initializeVolumesInChunk() {
        this.initializeData();
        HashSet<BlockPos> tmp = new HashSet<>();
        int cur_volume_id = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = this.chunk.getBottomY(); y <= this.getHighestY(x, z); y++) {
                    BlockPos tmp_pos = this.getChunkPos().getBlockPos(x, y, z);
                    if (!this.canContainGas(tmp_pos)) this.setVolumeIdAt(tmp_pos, -1);
                    else if (!tmp.contains(tmp_pos)) {
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

    public static @NotNull GasData initializeVolumesInChunk(@NotNull Chunk chunk, WorldView world) {
        if (GasData.currentlyInitializing.contains(chunk.getPos())) {
            LOGGER.warn("Attempt to initialize volumes in chunk {} in world {} while they are already being initialized", chunk.getPos(), world);
        }
        GasData.currentlyInitializing.add(chunk.getPos());
        GasData data = new GasData(world, chunk);
        data.initializeVolumesInChunk();
        LOGGER.info("initializing gas data for chunk {}", chunk.getPos());
        GasData.currentlyInitializing.remove(chunk.getPos());
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GasData gasData = (GasData) o;
        return initialized_data == gasData.initialized_data && Objects.equal(data, gasData.data) && Objects.equal(gas_data, gasData.gas_data) && Objects.equal(max_volume_id, gasData.max_volume_id) && Objects.equal(chunkPos, gasData.chunkPos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data, gas_data, max_volume_id, initialized_data, chunkPos);
    }
}
