package org.tarlaboratories.tartech;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.chemistry.ChemicalArgumentType;

import java.util.Set;
import java.util.function.Predicate;

public class ModCommands {
    public static final Predicate<ServerCommandSource> IS_OPERATOR = (ctx) -> ctx.hasPermissionLevel(2);
    public static final Command<ServerCommandSource> ADD_GAS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final @Nullable Entity entity = source.getEntity();

        return 0;
    };
    public static final Command<ServerCommandSource> ADD_GAS_AT_POS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        final Chemical gas = context.getArgument("gas", Chemical.class);
        final Double amount = DoubleArgumentType.getDouble(context, "amount");
        state.addGasAtPos(pos, gas, amount);
        return 0;
    };
    public static final Command<ServerCommandSource> TEST_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final ServerPlayerEntity player = source.getPlayerOrThrow();
        int dist = IntegerArgumentType.getInteger(context, "radius");
        StateSaverAndLoader state = StateSaverAndLoader.getWorldState(context.getSource().getWorld());
        Set<BlockPos> tmp = state.getDataForChunk(player.getChunkPos()).getConnectedBlocks(player.getBlockPos(), (p) -> false, dist);
        for (BlockPos pos : tmp) context.getSource().getWorld().setBlockState(pos, Blocks.RED_STAINED_GLASS.getDefaultState());
        return 0;
    };

    public static void initialize() {
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(Tartech.MOD_ID, "chemical_argument_type"),
                ChemicalArgumentType.class, ConstantArgumentSerializer.of(ChemicalArgumentType::chemical)
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("addgas")
                    .requires(IS_OPERATOR)
                    .then(CommandManager.argument("gas", ChemicalArgumentType.chemical())
                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(ADD_GAS_COMMAND)
                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                    .executes(ADD_GAS_AT_POS_COMMAND)))));
            dispatcher.register(CommandManager.literal("generate_sphere")
                    .then(CommandManager.argument("radius", IntegerArgumentType.integer())
                    .executes(TEST_COMMAND)));
        });
    }
}
