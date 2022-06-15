package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.utils.Constraints;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class CivilizationCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("civilization")
                .executes(CivilizationCommand::executeVersion)
                .then(literal("version").executes(CivilizationCommand::executeVersion))
                .then(BlueprintCommand.register())
                .then(CityCommand.register())
        );
    }

    private static int executeVersion(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(Text.of("%s %s".formatted(Constraints.MOD_NAME, Constraints.MOD_VERSION)), false);
        return 0;
    }
}
