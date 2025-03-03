package org.tarlaboratories.tartech.gas;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.ModFluids;
import org.tarlaboratories.tartech.StateSaverAndLoader;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.fluids.ChemicalFluid;
import org.tarlaboratories.tartech.networking.GasCondensationPayload;
import org.tarlaboratories.tartech.networking.LiquidEvaporationPayload;

import java.util.*;
import java.util.function.Predicate;

public class GasData {
    protected static HashSet<ChunkPos> currentlyInitializing = new HashSet<>();
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
            Codec.BOOL.fieldOf("needsUpdating").forGetter(GasData::needsUpdating),
            ChunkPos.CODEC.fieldOf("chunk_pos").forGetter(GasData::getChunkPos)
    ).apply(instance, GasData::new));

    public boolean needsUpdating() {
        return this.needsUpdating;
    }

    public Chunk chunk;
    protected boolean needsUpdating;
    protected ChunkPos chunkPos;
    protected RegistryKey<DimensionType> dimension;
    protected final HashMap<RegistryKey<DimensionType>, GasVolume> DEFAULT_GAS_VOLUMES = new HashMap<>(Map.of(
            DimensionTypes.OVERWORLD, (new GasVolume()).addGas(Chemical.OXYGEN, 0.2).addGas(Chemical.fromString("N2"), 0.8).setTemperature(20),
            DimensionTypes.THE_NETHER, (new GasVolume()).addGas(Chemical.SULFUR_DIOXIDE, 0.5).setTemperature(80),
            DimensionTypes.THE_END, (new GasVolume()).setTemperature(-50)
    ));

    private static final Logger LOGGER = LogManager.getLogger();

    protected GasData(@NotNull WorldView world, @NotNull Chunk chunk) {
        this.chunk = chunk;
        this.chunkPos = chunk.getPos();
        this.dimension = world.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE).getKey(world.getDimension()).orElseThrow();
    }

    protected GasData(List<List<List<Integer>>> data, Map<Integer, GasVolume> gas_data, int max_volume_id, int old_max_volume_id, boolean initialized_data, boolean needsUpdating, ChunkPos pos) {
        this.data = new ArrayList<>(data);
        this.gas_data = new HashMap<>(gas_data);
        this.max_volume_id = max_volume_id;
        this.old_max_volume_id = old_max_volume_id;
        this.initialized_data = initialized_data;
        this.needsUpdating = needsUpdating;
        this.chunkPos = pos;
    }

    public void deleteNotNeededData() {
        try {
            for (Integer i : this.gas_data.keySet()) {
                if (i <= this.old_max_volume_id) {
                    LOGGER.info("Removing volume with id {} from chunk {} as it is not needed anymore", i, this.chunkPos);
                    this.gas_data.remove(i);
                }
            }
        } catch (ConcurrentModificationException e) {
            LOGGER.warn("Could not trim gas data");
        }
    }

    protected Integer getOldMaxVolumeId() {
        return this.old_max_volume_id;
    }

    protected ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    protected List<List<List<Integer>>> getData() {
        return this.data;
    }

    protected Map<Integer, GasVolume> getGasData() {
        return this.gas_data;
    }

    protected Integer getMaxVolumeId() {
        return this.max_volume_id;
    }

    protected boolean isInitializedData() {
        return initialized_data;
    }

    /**
     * @return if {@code initializeVolumesInChunk} has started but not yet completed for the chunk at {@code pos}
     * @see GasData#initializeVolumesInChunk
     */
    public static boolean isCurrentlyInitializing(ChunkPos pos) {
        return currentlyInitializing.contains(pos);
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
            BlockState neighbour = this.chunk.getBlockState(pos.offset(direction));
            if (this.isInSameChunk(pos.offset(direction)) && (!this.chunk.getFluidState(pos).isEmpty() || !state.isSideSolidFullSquare(this.chunk, pos, direction)) &&
                    (!this.chunk.getFluidState(pos.offset(direction)).isEmpty() || !neighbour.isSideSolidFullSquare(this.chunk, pos.offset(direction), direction.getOpposite()))) out.add(pos.offset(direction));
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

    /**
     * @return an instance of {@code GasVolume} (changing it will change the state of gas) representing the state of gas at {@code pos}, if this volume is exposed to sky returns the default for this dimension type
     * @implNote does NOT trigger a gas update
     */
    public @NotNull GasVolume getGasVolumeAt(@NotNull BlockPos pos) {
        int tmp = this.getVolumeIdAt(pos);
        if (tmp == -1 && this.canContainGas(pos)) {
            if (this.canSeeSky(pos)) return this.getDefaultGasVolume().multiplyContentsBy(10000).addVolume(10000).exposed();
            LOGGER.warn("volume id is -1, but block can contain gas at pos = {}", pos);
            return (new GasVolume()).addVolume(1);
        } else if (tmp == -1) return new GasVolume();
        else if (tmp <= old_max_volume_id) return this.getDefaultGasVolume().multiplyContentsBy(10000).addVolume(10000).exposed();
        return this.gas_data.getOrDefault(tmp, new GasVolume());
    }

    /**
     * @return an instance of {@code GasVolume} (changing it will change the state of gas) representing the state of gas at {@code pos} in {@code world},
     * if this volume is exposed to sky returns the default for this dimension type
     * @implNote triggers a gas update for this chunk (may lag)
     * @see GasVolume
    */
    public static @NotNull GasVolume get(@NotNull BlockPos pos, @NotNull ServerWorld world) {
        GasData tmp = getEntityForChunk(world.getChunk(pos), world);
        if (tmp.needsUpdating()) tmp.updateVolumesInChunk(world);
        return tmp.getGasVolumeAt(pos);
    }

    /**
     * Should only be used for debugging purposes
     * @return the volume id at {@code pos} in {@code world}
     * @implNote does not trigger a gas update for this chunk
     */
    public static int getGasVolumeIdAt(BlockPos pos, ServerWorld world) {
        return getEntityForChunk(world.getChunk(pos), world).getVolumeIdAt(pos);
    }

    protected void setVolumeIdAt(@NotNull BlockPos pos, int id) {
        if (!this.initialized_data) initializeData();
        int i = pos.getX() - this.getChunkPos().getStartX(), j = pos.getY() - this.chunk.getBottomY(), k = pos.getZ() - this.getChunkPos().getStartZ();
        try {
            this.data.get(i).get(j).set(k, id);
        } catch (UnsupportedOperationException exception) {
            try {
                this.data.get(i).set(j, new ArrayList<>(this.data.get(i).get(j)));
            } catch (UnsupportedOperationException exception2) {
                this.data.set(i, new ArrayList<>(this.data.get(i)));
                this.data.get(i).set(j, new ArrayList<>(this.data.get(i).get(j)));
            }
            this.data.get(i).get(j).set(k, id);
        }
    }

    protected Set<BlockPos> setVolumeAtPos(@NotNull BlockPos pos, int id) {
        Set<BlockPos> tmp = this.getConnectedBlocks(pos);
        for (BlockPos pos1 : tmp) this.setVolumeIdAt(pos1, id);
        return tmp;
    }

    protected boolean canContainGas(@NotNull BlockPos pos) {
        assert !this.chunk.isOutOfHeightLimit(pos);
        return !this.getNeighboursWithSameGas(pos).isEmpty() || this.chunk.getBlockState(pos).isAir() || this.chunk.getFluidState(pos).isIn(ModFluids.CHEMICAL_FLUID_TAG);
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

    protected void updateVolumeInPos(@NotNull BlockPos pos, ServerWorld world){
        int prev_id = getGasVolumeIdAt(pos, world);

        if (prev_id == -1) {
            if (!canContainGas(pos)) return;
            List<BlockPos> neighbours = this.getNeighboursWithSameGas(pos);
            if(neighbours.isEmpty()) {
                max_volume_id++;
                setVolumeIdAt(pos, max_volume_id);
                this.gas_data.put(max_volume_id, new GasVolume().addVolume(1));
                return;
            }

            int max_size_id = getVolumeIdAt(neighbours.getFirst());
            for (BlockPos tmp_pos : neighbours) {
                if (getGasVolumeAt(tmp_pos).volume > gas_data.get(max_size_id).volume) {
                    max_size_id = getVolumeIdAt(tmp_pos);
                }
            }

            for(BlockPos tmp_pos : neighbours){
                if(getVolumeIdAt(tmp_pos) == max_size_id) continue;
                gas_data.get(max_size_id).mergeWith(getGasVolumeAt(tmp_pos));
                setVolumeAtPos(tmp_pos,max_size_id);
            }
            gas_data.get(max_size_id).volume--;
            return;
        }
        if(!canContainGas(pos)){
            if(getGasVolumeAt(pos).volume <= 1){
                gas_data.remove(getVolumeIdAt(pos));
            }
            setVolumeIdAt(pos,-1);
        }

    }


    protected Set<BlockPos> updateVolumeAtPos(@NotNull BlockPos pos, ServerWorld world) {
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
        this.gas_data.put(max_volume_id + 1, gasVolume.unexposed());
        max_volume_id++;
        for (Object o : connected_blocks.stream().sorted(Comparator.comparingInt(Vec3i::getY)).toArray()) {
            if (o instanceof BlockPos tmp_pos) {
                FluidState fluid = chunk.getFluidState(tmp_pos);
                if (fluid.getFluid() instanceof ChemicalFluid chemicalFluid) {
                    if (!chemicalFluid.getChemical().getProperties().canBeGas() || chemicalFluid.getChemical().getProperties().boilingTemperature() > gasVolume.getTemperature()) {
                        if (fluid.isStill()) gasVolume.addLiquid(fluid);
                    } else {
                        if (fluid.isStill()) gasVolume.addGas(chemicalFluid.getChemical(), 1);
                        chunk.setBlockState(tmp_pos, Blocks.AIR.getDefaultState(), false);
                        for (ServerPlayerEntity player : PlayerLookup.tracking(world, tmp_pos)) {
                            ServerPlayNetworking.send(player, new LiquidEvaporationPayload(tmp_pos));
                        }
                    }
                }
                if (gasVolume.to_be_liquefied.isEmpty() || !chunk.getBlockState(pos).isAir()) continue;
                Pair<Chemical, Double> tmp = gasVolume.getLiquidToBeLiquefied();
                if (Double.compare(tmp.getRight(), 1) >= 0) {
                    chunk.setBlockState(tmp_pos, ModFluids.CHEMICAL_FLUIDS.get(tmp.getLeft()).getLeft().getDefaultState().getBlockState(), false);
                    for (ServerPlayerEntity player : PlayerLookup.tracking(world, tmp_pos)) {
                        ServerPlayNetworking.send(player, new GasCondensationPayload(tmp_pos, tmp.getLeft().toString()));
                    }
                }
            } else LOGGER.error("wtf that shouldn't happen {}", o);
        }
        return connected_blocks;
    }

    /**
     * Updates all volumes in the chunk that this instance of {@code GasData} represents.
     * @implNote Has worst case {@code O(n)} complexity, where {@code n} is the number of blocks in this chunk. The worst case is when there are non-solid blocks at height limit.
     */
    public void updateVolumesInChunk(ServerWorld world) {
        Integer old_max_volume_id = this.max_volume_id;
        Set<BlockPos> tmp = new HashSet<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = this.chunk.getBottomY(); y <= this.getHighestY(x, z); y++) {
                    BlockPos tmp_pos = this.chunkPos.getBlockPos(x, y, z);
                    if (!this.canContainGas(tmp_pos)) this.setVolumeIdAt(tmp_pos, -1);
                    else if (!tmp.contains(tmp_pos)) tmp.addAll(this.updateVolumeAtPos(tmp_pos, world));
                }
            }
        }
        this.old_max_volume_id = old_max_volume_id;
        this.needsUpdating = false;
    }

    /**
     * Updates ONLY the volume that is directly connected to {@code pos}
     */
    @SuppressWarnings("unused")
    public static void updateVolumeAtPos(ServerWorld world, BlockPos pos) {
        getEntityForChunk(world.getChunk(pos), world).updateVolumeAtPos(pos, world);
    }

    @SuppressWarnings("unused")
    public static void updateVolumesInChunk(ServerWorld world, BlockPos pos) {
        getEntityForChunk(world.getChunk(pos), world).updateVolumesInChunk(world);
    }

    protected GasVolume getDefaultGasVolume() {
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

    /**
     * @return a new instance of {@code GasData} that contains the gas data for {@code chunk} in {@code world}
     * @implNote Does not change the state for this world in any way, except adding and removing this chunk from
     * {@code currentlyInitializing}, which is only used for warnings.
     * @see StateSaverAndLoader#reinitializeDataAtPos
     */
    public static @NotNull GasData initializeVolumesInChunk(@NotNull Chunk chunk, ServerWorld world) {
        if (GasData.currentlyInitializing.contains(chunk.getPos())) {
            LOGGER.warn("Attempt to initialize volumes in chunk {} in world {} while they are already being initialized", chunk.getPos(), world);
        }
        GasData.currentlyInitializing.add(chunk.getPos());
        GasData data = new GasData(world, chunk);
        data.initializeVolumesInChunk();
        data.updateVolumesInChunk(world);
        LOGGER.info("initializing gas data for chunk {}", chunk.getPos());
        GasData.currentlyInitializing.remove(chunk.getPos());
        return data;
    }

    public static boolean playerBreathe(@NotNull ServerPlayerEntity player) {
        BlockPos pos = BlockPos.ofFloored(player.getEyePos());
        if (player.isSubmergedIn(FluidTags.WATER) || player.isSubmergedIn(FluidTags.LAVA) || player.isSubmergedIn(ModFluids.CHEMICAL_FLUID_TAG)) return false;
        GasVolume gasVolume = get(pos, player.getServerWorld());
        boolean tmp = gasVolume.breathable();
        if (tmp) {
            gasVolume.removeGas(Chemical.OXYGEN, 0.001);
            gasVolume.addGas(Chemical.fromString("CO2"), 0.001);
        }
        return tmp;
    }

    public void markDirty() {
        this.needsUpdating = true;
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
