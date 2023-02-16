package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.ext.IServerWorldExt;
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
        return builder
                .then(literal("version").executes(wrapCommand(CivilizationCommand::execVersion)))
                .then(literal("help").executes(CivilizationCommand::execHelp))
                .then(literal("debug")
                        .then(
                                literal("personCatalog")
                                        .executes(wrapCommand(CivilizationCommand::execDebugPersonCatalogList))
                                        .then(literal("list").executes(wrapCommand(CivilizationCommand::execDebugPersonCatalogList)))
                                        .then(literal("reset").executes(wrapCommand(CivilizationCommand::execDebugPersonCatalogReset)))
                        )
                );
    }

    private static int execVersion(CommandContext<ServerCommandSource> ctx, ServerCommandSource src) {
        var titleStyle = Style.EMPTY.withBold(true).withColor(Formatting.AQUA);
        var linkStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, MOD_URL)).withUnderline(true);

        src.sendFeedback(Text.of("====================================================="), false);
        src.sendFeedback(Text.of(""), false);
        src.sendFeedback(MutableText.of(TextContent.EMPTY).append("    ").append(MutableText.of(TextContent.EMPTY).append("%s Mod".formatted(MOD_NAME)).setStyle(titleStyle)), false);
        src.sendFeedback(Text.of("      by %s".formatted(MOD_AUTHOR)), false);
        src.sendFeedback(Text.of(""), false);
        src.sendFeedback(MutableText.of(TextContent.EMPTY).append("    ").append(MutableText.of(TextContent.EMPTY).append(MOD_URL).setStyle(linkStyle)), false);
        src.sendFeedback(Text.of(""), false);
        src.sendFeedback(Text.of("    %s@%s  -  Minecraft %s".formatted(MOD_ID, MOD_VERSION, MC_VERSION)), false);
        src.sendFeedback(Text.of(""), false);
        src.sendFeedback(Text.of("====================================================="), false);
        return 0;
    }

    private static int execHelp(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(Text.of("TODO: Help"), false);
        return 0;
    }

    private static int execDebugPersonCatalogList(CommandContext<ServerCommandSource> ctx, ServerCommandSource src) {
        var personCatalog = ((IServerWorldExt) ctx.getSource().getWorld()).getPersonCatalog();

        src.sendFeedback(Text.of("Person Catalog (%d items):".formatted(personCatalog.getPeopleMap().size())), false);
        personCatalog.getPeopleMap().forEach((uuid, id) -> src.sendFeedback(Text.of("  %s -> %s".formatted(uuid, id)), false));

        return 0;
    }

    private static int execDebugPersonCatalogReset(CommandContext<ServerCommandSource> ctx, ServerCommandSource src) {
        var personCatalog = ((IServerWorldExt) ctx.getSource().getWorld()).getPersonCatalog();
        personCatalog.reset();
        src.sendFeedback(Text.of("Person Catalog reset!"), false);
        return 0;
    }
}
