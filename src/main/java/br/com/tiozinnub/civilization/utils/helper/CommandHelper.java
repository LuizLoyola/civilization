package br.com.tiozinnub.civilization.utils.helper;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandHelper {
    public static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        try {
            return context.getSource().getPlayer();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}
