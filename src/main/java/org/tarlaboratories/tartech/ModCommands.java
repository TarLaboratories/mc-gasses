package org.tarlaboratories.tartech;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.NumberRangeArgumentType.floatRange;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.chemistry.Chemical;
import org.tarlaboratories.tartech.chemistry.ChemicalArgumentType;

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
        final BlockPos pos = context.getArgument("pos", BlockPos.class);
        final Chemical gas = context.getArgument("gas", Chemical.class);
        final Double amount = context.getArgument("amount", Double.class);
        state.addGasAtPos(pos, gas, amount);
        return 0;
    };

    public static void initialize() {
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(Tartech.MOD_ID, "chemical_argument_type"),
                ChemicalArgumentType.class, ConstantArgumentSerializer.of(ChemicalArgumentType::chemical)
        );
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("addgas")
                    .then(argument("gas", ChemicalArgumentType.chemical()))
                    .then(argument("amount", floatRange()))
                    .requires(IS_OPERATOR).executes(ADD_GAS_COMMAND)
                    .then(argument("pos", blockPos()))
                    .executes(ADD_GAS_AT_POS_COMMAND));
        });
    }
}
