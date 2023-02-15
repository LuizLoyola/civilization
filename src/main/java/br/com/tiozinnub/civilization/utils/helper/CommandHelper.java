package br.com.tiozinnub.civilization.utils.helper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CommandHelper {
    public static Command<ServerCommandSource> wrapCommand(UnwrappedCommand<ServerCommandSource> command) {
        return (ctx) -> {
            var source = ctx.getSource();
            return command.run(ctx, source);
        };
    }

    public static Command<ServerCommandSource> wrapPlayerCommand(UnwrappedPlayerCommand<ServerCommandSource> command) {
        return (ctx) -> {
            var source = ctx.getSource();
            var player = source.getPlayer();

            if (player == null) throw new SimpleCommandExceptionType(Text.of("This command is player-only.")).create();

            return command.run(ctx, source, player);
        };
    }

    @FunctionalInterface
    public interface UnwrappedCommand<S> {
        int run(CommandContext<S> ctx, ServerCommandSource source) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface UnwrappedPlayerCommand<S> {
        int run(CommandContext<S> ctx, ServerCommandSource source, ServerPlayerEntity playerEntity) throws CommandSyntaxException;
    }
}
