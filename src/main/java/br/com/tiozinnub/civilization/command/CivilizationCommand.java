package br.com.tiozinnub.civilization.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import static br.com.tiozinnub.civilization.utils.Constraints.*;
import static br.com.tiozinnub.civilization.utils.helper.CommandHelper.wrapCommand;
import static net.minecraft.server.command.CommandManager.literal;

public class CivilizationCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(wrapRoot(literal("civilization").executes(wrapCommand(CivilizationCommand::execRoot))));
        dispatcher.register(wrapRoot(literal("civ").executes(wrapCommand(CivilizationCommand::execRoot))));
    }

    private static int execRoot(CommandContext<ServerCommandSource> ctx, ServerCommandSource source) {
        // default command when executing root
        return execVersion(ctx, source);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> wrapRoot(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder.then(literal("version").executes(wrapCommand(CivilizationCommand::execVersion))).then(literal("help").executes(CivilizationCommand::execHelp));
    }

    private static int execVersion(CommandContext<ServerCommandSource> ctx, ServerCommandSource source) {
        var titleStyle = Style.EMPTY.withBold(true).withColor(Formatting.AQUA);
        var linkStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, MOD_URL)).withUnderline(true);

        ctx.getSource().sendFeedback(Text.of("====================================================="), false);
        ctx.getSource().sendFeedback(Text.of(""), false);
        ctx.getSource().sendFeedback(MutableText.of(TextContent.EMPTY).append("    ").append(MutableText.of(TextContent.EMPTY).append("%s Mod".formatted(MOD_NAME)).setStyle(titleStyle)), false);
        ctx.getSource().sendFeedback(Text.of("      by %s".formatted(MOD_AUTHOR)), false);
        ctx.getSource().sendFeedback(Text.of(""), false);
        ctx.getSource().sendFeedback(MutableText.of(TextContent.EMPTY).append("    ").append(MutableText.of(TextContent.EMPTY).append(MOD_URL).setStyle(linkStyle)), false);
        ctx.getSource().sendFeedback(Text.of(""), false);
        ctx.getSource().sendFeedback(Text.of("    %s@%s  -  Minecraft %s".formatted(MOD_ID, MOD_VERSION, MC_VERSION)), false);
        ctx.getSource().sendFeedback(Text.of(""), false);
        ctx.getSource().sendFeedback(Text.of("====================================================="), false);
        return 0;
    }

    private static int execHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(Text.of("TODO: Help"), false);
        return 0;
    }

}
