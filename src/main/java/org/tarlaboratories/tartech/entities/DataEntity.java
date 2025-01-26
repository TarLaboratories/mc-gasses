package org.tarlaboratories.tartech.entities;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.GasVolume;

import java.util.ArrayList;
import java.util.List;

public class DataEntity extends MobEntity {
    protected ArrayList<ArrayList<ArrayList<Integer>>> data;
    private ArrayList<GasVolume> gas_data;

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
        gas_data = new ArrayList<>(Codec.list(GasVolume.CODEC).parse(NbtOps.INSTANCE, nbt.get("gas_data")).result().orElse(List.of()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
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
}
