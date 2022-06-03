package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static br.com.tiozinnub.civilization.utils.helper.CommandHelper.getPlayer;
import static net.minecraft.server.command.CommandManager.literal;

public class LayoutCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register() {
        return literal("layout")
                .executes(LayoutCommand::executeGet);
    }

    private static int executeGet(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        var player = getPlayer(context);
        if (player == null) {
            source.sendError(Text.of("You must be a player to execute this command."));
            return 1;
        }

        var cityManager = ((IServerWorldExt) world).getCityManager();

        var city = cityManager.getCityAt(player.getBlockPos());

        if (city == null) {
            source.sendError(Text.of("You are not in a city."));
            return 1;
        }

        var layoutPart = city.getLayoutPartAt(player.getBlockPos());
        if (layoutPart == null) {
            source.sendError(Text.of("You are not in a layout part."));
            return 1;
        }
        source.sendFeedback(Text.of("You are at a %s.".formatted(layoutPart.getClass().getSimpleName())), false);
        source.sendFeedback(layoutPart.getDescription(), false);
        return 0;
    }
}
