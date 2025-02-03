package org.tarlaboratories.tartech.entities;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.tarlaboratories.tartech.GasVolume;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.chemistry.ChemicalElement;

import java.util.*;
import java.util.function.Predicate;

import static org.tarlaboratories.tartech.ModEntities.DATA_ENTITY;

public class DataEntity extends MobEntity {
    public static HashSet<ChunkPos> currentlyInitializing = new HashSet<>();
    protected ArrayList<ArrayList<ArrayList<Integer>>> data;
    protected ArrayList<GasVolume> gas_data;
    private boolean initialized_data = false;
    private final DynamicRegistryManager registryManager = this.getEntityWorld().getRegistryManager();
    protected final HashMap<DimensionType, GasVolume> DEFAULT_GAS_VOLUMES = new HashMap<>(Map.of(
            Objects.requireNonNull(registryManager.getOrThrow(RegistryKeys.DIMENSION_TYPE).get(DimensionTypes.OVERWORLD)), (new GasVolume()).addGas(Chemical.OXYGEN, 0.2).addGas(Chemical.primitiveOf(ChemicalElement.NITROGEN.getElement()), 0.8).setTemperature(20),
            Objects.requireNonNull(registryManager.getOrThrow(RegistryKeys.DIMENSION_TYPE).get(DimensionTypes.THE_NETHER)), (new GasVolume()).addGas(Chemical.SULFUR_DIOXIDE, 0.5).setTemperature(80),
            Objects.requireNonNull(registryManager.getOrThrow(RegistryKeys.DIMENSION_TYPE).get(DimensionTypes.THE_END)), (new GasVolume()).setTemperature(-50)
    ));

    private static final Logger LOGGER = LogManager.getLogger();

    public DataEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public void readCustomDataFromNbt(@NotNull NbtCompound nbt) {
        this.data = new ArrayList<>();
        int i = 0;
        while (nbt.contains(Integer.toString(i))) {
            ArrayList<ArrayList<Integer>> tmp2 = new ArrayList<>();
            int j = 0;
            while (nbt.contains(Integer.toString(j))) {
                ArrayList<Integer> tmp = new ArrayList<>();
                int k = 0;
                while (nbt.contains(Integer.toString(k))) {
                    tmp.add(nbt.getInt(Integer.toString(k)));
                    k++;
                }
                tmp2.add(tmp);
                j++;
            }
            this.data.add(tmp2);
            i++;
        }
        this.gas_data = new ArrayList<>(Codec.list(GasVolume.CODEC).parse(NbtOps.INSTANCE, nbt.get("gas_data")).result().orElse(List.of()));
        this.initialized_data = true;
    }

    @Override
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
        nbt.put("gas_data", Codec.list(GasVolume.CODEC).encodeStart(NbtOps.INSTANCE, gas_data).result().orElse(new NbtCompound()));
    }

    protected void initializeData() {
        this.data = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            this.data.add(new ArrayList<>());
            for (int j = 0; j < this.getEntityWorld().getHeight(); j++) {
                this.data.get(i).add(new ArrayList<>());
                for (int k = 0; k < 16; k++) this.data.get(i).get(j).add(-1);
            }
        }
        this.gas_data = new ArrayList<>();
        this.initialized_data = true;
    }

    protected List<BlockPos> getNeighboursWithSameGas(@NotNull BlockPos pos) {
        List<BlockPos> out = new ArrayList<>();
        BlockState state = this.getWorld().getBlockState(pos);
        for (Direction direction : List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH)) {
            if (this.isAboveHeightLimit(pos.offset(direction))) continue;
            if (this.isInSameChunk(pos.offset(direction)) && !state.isSideSolidFullSquare(this.getWorld(), pos, direction) &&
                    !this.getWorld().getBlockState(pos.offset(direction)).isSideSolidFullSquare(this.getWorld(), pos.offset(direction), direction.getOpposite())) out.add(pos.offset(direction));
        }
        return out;
    }

    protected List<BlockPos> getNeighboursWithDifferentGas(@NotNull BlockPos pos) {
        List<BlockPos> out = new ArrayList<>();
        BlockState state = this.getWorld().getBlockState(pos);
        for (Direction direction : List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH)) {
            if (this.isAboveHeightLimit(pos.offset(direction))) continue;
            if (this.isInSameChunk(pos.offset(direction)) && (state.isSideSolidFullSquare(this.getWorld(), pos, direction) ||
                    this.getWorld().getBlockState(pos.offset(direction)).isSideSolidFullSquare(this.getWorld(), pos.offset(direction), direction.getOpposite()))) out.add(pos.offset(direction));
        }
        return out;
    }

    protected boolean isInSameChunk(@NotNull BlockPos pos) {
        return this.getChunkPos().getStartX() <= pos.getX() && this.getChunkPos().getStartZ() <= pos.getZ() &&
                this.getChunkPos().getEndX() > pos.getX() && this.getChunkPos().getEndZ() > pos.getZ();
    }

    protected boolean isAboveHeightLimit(@NotNull BlockPos pos) {
        return this.getEntityWorld().getBottomY() + this.getEntityWorld().getHeight() <= pos.getY();
    }

    protected int getVolumeIdAt(@NotNull BlockPos pos) {
        if (!this.initialized_data) initializeData();
        return this.data.get(pos.getX() - this.getChunkPos().getStartX()).get(Math.min(Math.max(pos.getY() - this.getEntityWorld().getBottomY(), 0), 255)).get(pos.getZ() - this.getChunkPos().getStartZ());
    }

    protected GasVolume getGasVolumeAt(@NotNull BlockPos pos) {
        int tmp = this.getVolumeIdAt(pos);
        if (tmp == -1 && this.canContainGas(pos)) {
            LOGGER.warn("volume id is -1, but block can contain gas at pos = {}", pos);
            return (new GasVolume()).addVolume(1);
        }
        return this.gas_data.get(this.getVolumeIdAt(pos));
    }

    public static GasVolume getGasVolumeAt(@NotNull BlockPos pos, @NotNull World world) {
        DataEntity tmp = getEntityForChunk(world.getChunk(pos).getPos(), world);
        if (tmp == null) return new GasVolume();
        return tmp.getGasVolumeAt(pos);
    }

    protected void setVolumeIdAt(@NotNull BlockPos pos, int id) {
        if (!this.initialized_data) initializeData();
        this.data.get(pos.getX() - this.getChunkPos().getStartX()).get(pos.getY() - this.getEntityWorld().getBottomY()).set(pos.getZ() - this.getChunkPos().getStartZ(), id);
    }

    protected Set<BlockPos> setVolumeAtPos(@NotNull BlockPos pos, int id) {
        Set<BlockPos> tmp = this.getConnectedBlocks(pos);
        for (BlockPos pos1 : tmp) this.setVolumeIdAt(pos1, id);
        return tmp;
    }

    protected boolean canContainGas(@NotNull BlockPos pos) {
        return !this.getNeighboursWithSameGas(pos).isEmpty() || this.getWorld().isAir(pos);
    }

    protected Set<BlockPos> getConnectedBlocks(@NotNull BlockPos pos, @NotNull Predicate<BlockPos> predicate, @Range(from = 1, to = 1000) int max_dist) {
        Queue<BlockPos> q = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        if (!this.isInSameChunk(pos) || this.isAboveHeightLimit(pos) || !this.canContainGas(pos)) return visited;
        q.offer(pos); visited.add(pos);
        while (!q.isEmpty()) {
            BlockPos tmp = q.poll();
            for (BlockPos neighbour : this.getNeighboursWithSameGas(tmp)) {
                assert this.getWorld().getBlockState(neighbour).isTransparent();
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
        List<BlockPos> neighbours = this.getNeighboursWithSameGas(pos);
        int volume_id = this.getVolumeIdAt(pos);
        if (volume_id == -1 && this.canContainGas(pos)) {
            this.setVolumeIdAt(pos, this.gas_data.size());
            this.gas_data.add(new GasVolume());
        }
        if (volume_id == -1) return;
        for (BlockPos neighbour : neighbours) {
            if (volume_id != this.getVolumeIdAt(neighbour)) {
                this.gas_data.get(volume_id).mergeWith(this.getGasVolumeAt(pos));
                this.gas_data.set(this.getVolumeIdAt(neighbour), new GasVolume());
                this.setVolumeIdAt(neighbour, volume_id);
            }
        }
        List<BlockPos> neighbours2 = this.getNeighboursWithDifferentGas(pos);
        Set<BlockPos> tmp = new HashSet<>();
        for (BlockPos neighbour : neighbours2) {
            if (!neighbours.isEmpty()) tmp = this.getConnectedBlocks(neighbour, Predicate.isEqual(neighbours.get(0)), 5);
            if (!neighbours.isEmpty() && tmp.contains(neighbours.get(0))) continue;
            this.setVolumeAtPos(neighbour, this.gas_data.size());
            this.gas_data.add(this.getGasVolumeAt(pos).getPart(tmp.size()));
        }
    }

    public static void updateVolumeAtPos(@NotNull BlockPos pos, @NotNull World world) {
        DataEntity tmp = getEntityForChunk(world.getChunk(pos).getPos(), world);
        if (tmp != null) tmp.updateVolumeAtPos(pos);
    }

    public GasVolume getDefaultGasVolume() {
        if (DEFAULT_GAS_VOLUMES.containsKey(this.getWorld().getDimension())) {
            return DEFAULT_GAS_VOLUMES.get(this.getWorld().getDimension()).copy();
        } else {
            return DEFAULT_GAS_VOLUMES.get(Objects.requireNonNull(registryManager.getOrThrow(RegistryKeys.DIMENSION_TYPE).get(DimensionTypes.OVERWORLD))).copy();
        }
    }

    protected void updateVolumesInChunk() {
        this.initializeData();
        HashSet<BlockPos> tmp = new HashSet<>();
        int cur_volume_id = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = this.getEntityWorld().getBottomY(); y < this.getEntityWorld().getHeight() + this.getEntityWorld().getBottomY(); y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos tmp_pos = this.getChunkPos().getBlockPos(x, y, z);
                    if (!tmp.contains(tmp_pos)) {
                        HashSet<BlockPos> updated_blocks = new HashSet<>(this.setVolumeAtPos(tmp_pos, cur_volume_id));
                        this.gas_data.add(this.getDefaultGasVolume().addVolume(updated_blocks.size()).multiplyContentsBy(updated_blocks.size()));
                        tmp.addAll(updated_blocks);
                        cur_volume_id++;
                    }
                }
            }
        }
    }

    protected static int getMiddleY(@NotNull World world) {
        return world.getHeight()/2 - world.getBottomY();
    }

    protected static DataEntity getEntityForChunk(@NotNull ChunkPos chunkPos, World world) {
        BlockPos entity_pos = chunkPos.getCenterAtY(getMiddleY(world));
        List<DataEntity> tmp = world.getEntitiesByClass(DataEntity.class, Box.enclosing(entity_pos, entity_pos), (e) -> true);
        assert tmp.size() < 2;
        if (tmp.isEmpty()) {
            return initializeVolumesInChunk(chunkPos, world);
        } else return tmp.get(0);
    }

    public static @Nullable DataEntity initializeVolumesInChunk(@NotNull ChunkPos chunkPos, World world) {
        BlockPos entity_pos = chunkPos.getCenterAtY(getMiddleY(world));
        List<DataEntity> tmp = world.getEntitiesByClass(DataEntity.class, Box.enclosing(entity_pos, entity_pos), (e) -> true);
        assert tmp.size() < 2;
        if (!tmp.isEmpty()) return tmp.get(0);
        if (DataEntity.currentlyInitializing.contains(chunkPos)) {
            LOGGER.warn("Attempt to initialize volumes in chunk {} in world {} while they are already being initialized", chunkPos, world);
            return null;
        }
        DataEntity.currentlyInitializing.add(chunkPos);
        DataEntity entity = new DataEntity(DATA_ENTITY, world);
        world.spawnEntity(entity);
        entity.setPosition(entity_pos.toCenterPos());
        entity.updateVolumesInChunk();
        LOGGER.debug("new data entity is at BlockPos {}", entity_pos);
        DataEntity.currentlyInitializing.remove(chunkPos);
        return entity;
    }
}
