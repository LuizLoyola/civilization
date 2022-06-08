package br.com.tiozinnub.civilization.utils.helper;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class CommandHelper {
    public static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        return context.getSource().getPlayer();
    }

    public static BlockPos getBlockPos(CommandContext<ServerCommandSource> context, String parameter) {
        try {
            return BlockPosArgumentType.getBlockPos(context, parameter);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}
