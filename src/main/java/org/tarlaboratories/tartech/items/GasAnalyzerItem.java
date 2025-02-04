package org.tarlaboratories.tartech.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.GasVolume;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.GasData;

public class GasAnalyzerItem extends Item {
    public GasAnalyzerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(@NotNull World _world, PlayerEntity player, Hand hand) {
        if (_world instanceof ServerWorld world) {
            if (GasData.currentlyInitializing.contains(player.getChunkPos())) {
                player.sendMessage(Text.of("Please wait..."), true);
                return ActionResult.FAIL;
            }
            GasVolume data = GasData.getGasVolumeAt(player.getBlockPos(), world);
            player.sendMessage(Text.of("Gas info:"), false);
            player.sendMessage(Text.of(String.format("Temperature: %f", data.getTemperature())), false);
            player.sendMessage(Text.of(String.format("Pressure: %f", data.getPressure())), false);
            if (data.getVolume() <= 1000)
                player.sendMessage(Text.of(String.format("Volume: %d B", data.getVolume())), false);
            else
                player.sendMessage(Text.of("Volume: >1000 B"), false);
            player.sendMessage(Text.of("Gasses present in this volume:"), false);
            for (Chemical gas : data.getContents().keySet()) {
                if (data.getVolume() > 1000) player.sendMessage(Text.of(String.format("%s: %.2f%%", gas.toString(), data.getGasAmount(gas) * 100 / data.getVolume())), false);
                else player.sendMessage(Text.of(String.format("%s: %d mB (%.2f%%)", gas.toString(), Math.round(data.getGasAmount(gas) * 1000), data.getGasAmount(gas) * 100 / data.getVolume())), false);
            }
            return ActionResult.SUCCESS;
        } else return ActionResult.PASS;
    }
}
