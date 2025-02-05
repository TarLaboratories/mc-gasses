package org.tarlaboratories.tartech.chemistry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ChemicalArgumentType implements ArgumentType<Chemical> {
    public static final DynamicCommandExceptionType INVALID_CHEMICAL_STRING = new DynamicCommandExceptionType(o -> Text.literal(String.format("Invalid chemical: %s", o)));
    @Override
    public Chemical parse(@NotNull StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor();
        if (!reader.canRead()) reader.skip();
        while (reader.canRead() && Character.isLetterOrDigit(reader.peek()) || reader.peek() == '(' || reader.peek() == ')') {
            reader.skip();
        }
        String argString = reader.getString().substring(argBeginning, reader.getCursor());
        try {
            return Chemical.fromString(argString);
        } catch (InvalidChemicalStringException ex) {
            reader.setCursor(argBeginning);
            throw INVALID_CHEMICAL_STRING.createWithContext(reader, ex.getMessage());
        }
    }

    public static ArgumentType<Chemical> chemical() {
        return new ChemicalArgumentType();
    }
}
