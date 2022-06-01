package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.utils.Constraints;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CivilizationCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("citizenship")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.of("%s %s".formatted(Constraints.MOD_NAME, Constraints.MOD_VERSION)), false);
                    return 0;
                })
        );
    }
}
