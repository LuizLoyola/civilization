package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.utils.Constraints;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class CivilizationCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("civilization")
                .executes(CivilizationCommand::executeVersion)
                .then(literal("version").executes(CivilizationCommand::executeVersion))
                .then(CityCommand.register())
                .then(LayoutCommand.register())
        );
    }

    private static int executeVersion(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(Text.of("%s %s".formatted(Constraints.MOD_NAME, Constraints.MOD_VERSION)), false);
        return 0;
    }
}
