package org.tarlaboratories.tartech;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.chemistry.ChemicalArgumentType;
import org.tarlaboratories.tartech.gas.GasData;

import java.util.function.Predicate;

public class ModCommands {
    public static final Predicate<ServerCommandSource> IS_OPERATOR = (ctx) -> ctx.hasPermissionLevel(2);
    public static final Command<ServerCommandSource> ADD_GAS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final Entity entity = source.getEntityOrThrow();
        final BlockPos pos = entity.getBlockPos();
        final Chemical gas = context.getArgument("gas", Chemical.class);
        final Double amount = DoubleArgumentType.getDouble(context, "amount");
        state.addGasAtPos(pos, gas, amount);
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

    public static final Command<ServerCommandSource> GET_TEMPERATURE_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = source.getEntityOrThrow().getBlockPos();
        double temperature = state.getGasVolumeAtPos(pos).getTemperature();
        source.sendFeedback(() -> Text.literal(String.format("Temperature: %f", temperature)), false);
        return (int) Math.round(temperature);
    };

    public static final Command<ServerCommandSource> GET_TEMPERATURE_AT_POS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        double temperature = state.getGasVolumeAtPos(pos).getTemperature();
        source.sendFeedback(() -> Text.literal(String.format("Temperature: %f", temperature)), false);
        return (int) Math.round(temperature);
    };

    public static final Command<ServerCommandSource> GET_GAS_AMOUNT_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = source.getEntityOrThrow().getBlockPos();
        final Chemical gas = context.getArgument("gas", Chemical.class);
        double amount = state.getGasVolumeAtPos(pos).getGasAmount(gas);
        source.sendFeedback(() -> Text.literal(String.format("Amount of %s in current volume: %f", gas.toString(), amount)), false);
        return (int) Math.round(amount);
    };

    public static final Command<ServerCommandSource> GET_GAS_AMOUNT_AT_POS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        final Chemical gas = context.getArgument("gas", Chemical.class);
        double amount = state.getGasVolumeAtPos(pos).getGasAmount(gas);
        source.sendFeedback(() -> Text.literal(String.format("Amount of %s in current volume: %f", gas.toString(), amount)), false);
        return (int) Math.round(amount);
    };

    public static final Command<ServerCommandSource> GET_GAS_INFO_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = source.getEntityOrThrow().getBlockPos();
        source.sendFeedback(() -> state.getGasVolumeAtPos(pos).getInfo(true), false);
        return 0;
    };

    public static final Command<ServerCommandSource> GET_GAS_INFO_AT_POS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        source.sendFeedback(() -> state.getGasVolumeAtPos(pos).getInfo(true), false);
        return 0;
    };

    public static final Command<ServerCommandSource> UPDATE_GAS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = source.getEntityOrThrow().getBlockPos();
        state.updateVolumesInChunk(pos);
        return 0;
    };

    public static final Command<ServerCommandSource> UPDATE_GAS_AT_POS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        state.updateVolumesInChunk(pos);
        return 0;
    };

    public static final Command<ServerCommandSource> REINIT_GAS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = source.getEntityOrThrow().getBlockPos();
        state.reinitializeDataAtPos(pos);
        return 0;
    };

    public static final Command<ServerCommandSource> REINIT_GAS_AT_POS_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final StateSaverAndLoader state = StateSaverAndLoader.getWorldState(source.getWorld());
        final BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        state.reinitializeDataAtPos(pos);
        return 0;
    };

    public static final Command<ServerCommandSource> GET_VOLUME_ID_COMMAND = (context) -> {
        final ServerCommandSource source = context.getSource();
        final BlockPos pos = source.getEntityOrThrow().getBlockPos();
        source.sendFeedback(() -> Text.of(String.format("Volume id at %s: %d", pos.toString(), GasData.getGasVolumeIdAt(pos, source.getWorld()))), false);
        return 0;
    };

    public static void addHeat(ServerCommandSource source, BlockPos pos, double heat) {
        GasData.get(pos, source.getWorld()).addHeat(heat);
        GasData.doLiquidCheckForChunk(source.getWorld(), pos);
    }

    public static final Command<ServerCommandSource> ADD_HEAT_COMMAND = (context) -> {
        addHeat(context.getSource(), context.getSource().getEntityOrThrow().getBlockPos(), DoubleArgumentType.getDouble(context, "heat"));
        return 0;
    };

    public static final Command<ServerCommandSource> ADD_HEAT_AT_POS_COMMAND = (context) -> {
        addHeat(context.getSource(), BlockPosArgumentType.getBlockPos(context, "pos"), DoubleArgumentType.getDouble(context, "heat"));
        return 0;
    };

    public static void initialize() {
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(Tartech.MOD_ID, "chemical_argument_type"),
                ChemicalArgumentType.class, ConstantArgumentSerializer.of(ChemicalArgumentType::chemical)
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("gas")
                .then(CommandManager.literal("add")
                        .requires(IS_OPERATOR)
                        .then(CommandManager.argument("gas", ChemicalArgumentType.chemical())
                                .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ADD_GAS_COMMAND)
                                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(ADD_GAS_AT_POS_COMMAND))))
                        .then(CommandManager.literal("heat")
                                .then(CommandManager.argument("heat", DoubleArgumentType.doubleArg())
                                        .executes(ADD_HEAT_COMMAND)
                                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(ADD_HEAT_AT_POS_COMMAND)))))
                .then(CommandManager.literal("get")
                        .requires(IS_OPERATOR)
                        .then(CommandManager.literal("temperature")
                                .executes(GET_TEMPERATURE_COMMAND)
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(GET_TEMPERATURE_AT_POS_COMMAND)))
                        .then(CommandManager.literal("gas_amount")
                                .then(CommandManager.argument("gas", ChemicalArgumentType.chemical())
                                        .executes(GET_GAS_AMOUNT_COMMAND)
                                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(GET_GAS_AMOUNT_AT_POS_COMMAND))))
                        .then(CommandManager.literal("volume_id")
                                .executes(GET_VOLUME_ID_COMMAND))
                        .then(CommandManager.literal("info")
                                .executes(GET_GAS_INFO_COMMAND)
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(GET_GAS_INFO_AT_POS_COMMAND))))
                .then(CommandManager.literal("update")
                        .requires(IS_OPERATOR)
                        .executes(UPDATE_GAS_COMMAND)
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(UPDATE_GAS_AT_POS_COMMAND)))
                .then(CommandManager.literal("regenerate")
                        .requires(IS_OPERATOR)
                        .executes(REINIT_GAS_COMMAND)
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(REINIT_GAS_AT_POS_COMMAND)))
        ));
    }
}
