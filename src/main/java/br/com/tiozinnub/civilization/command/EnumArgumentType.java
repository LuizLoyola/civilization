package br.com.tiozinnub.civilization.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<TEnum extends Enum<TEnum>> implements ArgumentType<TEnum> {
    private final Class<TEnum> type;

    private EnumArgumentType(Class<TEnum> type) {
        this.type = type;
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enumArg(Class<T> type) {
        return new EnumArgumentType<T>(type);
    }

    public static <T extends Enum<T>> T getEnum(final CommandContext<?> context, Class<T> type, final String name) {
        return context.getArgument(name, type);
    }

    @Override
    public TEnum parse(final StringReader reader) throws CommandSyntaxException {
        return TEnum.valueOf(type, reader.readString().toUpperCase(Locale.ROOT));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        getExamples().stream().filter(e -> e.startsWith(builder.getRemainingLowerCase())).forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(type.getEnumConstants()).map(v -> v.name().toLowerCase()).toList();
    }
}

