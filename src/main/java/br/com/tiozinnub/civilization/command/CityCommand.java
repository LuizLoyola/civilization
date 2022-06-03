package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static br.com.tiozinnub.civilization.utils.helper.CommandHelper.getPlayer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CityCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register() {
        return literal("city")
                .executes(CityCommand::executeGet)
                .then(literal("create").executes(CityCommand::executeCreate))
                .then(literal("delete").executes(CityCommand::executeDeleteHere)
                        .then(literal("here").executes(CityCommand::executeDeleteHere))
                        .then(literal("at").then(argument("position", BlockPosArgumentType.blockPos()).executes(CityCommand::executeDeleteAt)))
                        .then(literal("all").executes(CityCommand::executeDeleteAll))
                )
                .then(literal("get").executes(CityCommand::executeGet));
    }

    private static int executeDeleteHere(CommandContext<ServerCommandSource> context) {
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

        city.markForDeletion();
        source.sendFeedback(Text.of("City %s deleted.".formatted(city.getName())), false);
        return 0;
    }

    private static int executeDeleteAll(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();

        var cityManager = ((IServerWorldExt) world).getCityManager();
        var count = 0;

        for (var city : cityManager.getCities()) {
            city.markForDeletion();
            source.sendFeedback(Text.of("City %s deleted.".formatted(city.getName())), false);
            count++;
        }

        source.sendFeedback(Text.of("%s cities deleted.".formatted(count)), false);

        return 0;
    }

    private static int executeDeleteAt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var world = source.getWorld();

        var pos = BlockPosArgumentType.getBlockPos(context, "position");

        var cityManager = ((IServerWorldExt) world).getCityManager();

        var city = cityManager.getCityAt(pos);

        if (city == null) {
            source.sendError(Text.of("You are not in a city."));
            return 1;
        }

        city.markForDeletion();
        source.sendFeedback(Text.of("City %s deleted.".formatted(city.getName())), false);
        return 0;
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

        source.sendFeedback(Text.of("You are at %s".formatted(city.getName())), false);
        return 0;
    }

    private static int executeCreate(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();
        var player = getPlayer(context);
        if (player == null) {
            source.sendError(Text.of("You must be a player to execute this command."));
            return 1;
        }

        var cityManager = ((IServerWorldExt) world).getCityManager();

        cityManager.createCity(player.getBlockPos());

        source.sendFeedback(Text.of("City created!"), false);
        return 0;
    }
}
