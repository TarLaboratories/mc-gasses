package org.tarlaboratories.tartech.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.tarlaboratories.tartech.gas.GasVolume;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.gas.GasData;

public class GasAnalyzerItem extends Item {
    public GasAnalyzerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(@NotNull World _world, PlayerEntity player, Hand hand) {
        if (_world instanceof ServerWorld world) {
            if (GasData.isCurrentlyInitializing(player.getChunkPos())) {
                player.sendMessage(Text.of("Please wait..."), true);
                return ActionResult.FAIL;
            }
            GasVolume data = GasData.get(player.getBlockPos(), world);
            player.sendMessage(data.getInfo(player.isCreative()), false);
            return ActionResult.SUCCESS;
        } else return ActionResult.PASS;
    }
}
