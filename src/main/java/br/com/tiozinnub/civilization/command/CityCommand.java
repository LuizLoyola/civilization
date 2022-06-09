package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.core.City;
import br.com.tiozinnub.civilization.core.structure.StructureType;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Function;

import static br.com.tiozinnub.civilization.command.CityCommand.CityGetType.*;
import static br.com.tiozinnub.civilization.utils.helper.CommandHelper.getBlockPos;
import static br.com.tiozinnub.civilization.utils.helper.CommandHelper.getPlayer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CityCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register() {
        return wrapCity(literal("city"), HERE).then(literal("create").then(argument("city_name", StringArgumentType.string()).executes(CityCommand::executeCreate))).then(literal("list").executes(CityCommand::executeList)).then(literal("at").then(wrapCity(argument("position", BlockPosArgumentType.blockPos()), AT))).then(literal("named").then(wrapCity(argument("name", StringArgumentType.string()), NAMED))).then(wrapCity(literal("here"), HERE));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> wrapCity(ArgumentBuilder<ServerCommandSource, ?> builder, CityGetType type) {
        return builder.executes(ctx -> getCityAndExecute(ctx, type, city -> executeInfo(ctx, city)))
                .then(literal("delete").executes(ctx -> getCityAndExecute(ctx, type, city -> executeDelete(ctx, city))))
                .then(literal("structure")
                        .then(literal("add")
                                .then(argument("structure_type", StringArgumentType.string()).executes(ctx -> getCityAndExecute(ctx, type, city -> executeStructureAdd(ctx, city))))
                        )
                );
    }

    private static Integer executeStructureAdd(CommandContext<ServerCommandSource> ctx, City city) {
        var structureType = StructureType.byName(StringArgumentType.getString(ctx, "structure_type"));
        if (structureType == null) {
            ctx.getSource().sendError(Text.of("Invalid structure type"));
            return 0;
        }

        city.addStructure(structureType);

        return 1;
    }

    private static int getCityAndExecute(CommandContext<ServerCommandSource> context, CityGetType type, Function<City, Integer> function) {
        var source = context.getSource();

        try {
            City city = null;
            BlockPos location = null;
            String name = null;

            if (type == HERE) {
                var player = getPlayer(context);
                if (player == null) {
                    source.sendError(Text.of("You must be a player to execute this command."));
                    return 1;
                }
                location = player.getBlockPos();
            }

            if (type == AT) {
                location = getBlockPos(context, "position");
                if (location == null) {
                    source.sendError(Text.of("Invalid position."));
                    return 1;
                }
            }

            if (type == NAMED) {
                name = StringArgumentType.getString(context, "name");
                if (name == null) {
                    source.sendError(Text.of("Invalid name."));
                    return 1;
                }
            }

            var world = source.getWorld();
            var cityManager = ((IServerWorldExt) world).getCityManager();

            if (location != null) {
                city = cityManager.getCityAt(location);
            } else if (name != null) {
                city = cityManager.getCity(name, false);
            }

            if (city == null) {
                if (type == HERE) source.sendError(Text.of("No city here."));
                if (type == AT) source.sendError(Text.of("No city at this position."));
                if (type == NAMED) source.sendError(Text.of("No city named " + name + "."));
                return 1;
            }

            return function.apply(city);
        } catch (Exception e) {
            source.sendError(Text.of("An error occurred."));
            source.sendError(Text.of(e.getMessage()));
            e.printStackTrace();
            return 1;
        }
    }

    private static int executeDelete(CommandContext<ServerCommandSource> ctx, City city) {
        city.markForDeletion();
        ctx.getSource().sendFeedback(Text.of("City " + city.getName() + " deleted."), false);
        return 0;
    }

    private static Integer executeInfo(CommandContext<ServerCommandSource> ctx, City city) {
        ctx.getSource().sendFeedback(Text.of(city.getName()), false);
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

        var cityName = context.getArgument("city_name", String.class);

        var cityManager = ((IServerWorldExt) world).getCityManager();

        try {
            cityManager.createCity(player.getBlockPos(), cityName);
            source.sendFeedback(Text.of("City %s created!".formatted(cityName)), false);
            return 0;
        } catch (IllegalArgumentException e) {
            source.sendError(Text.of(e.getMessage()));
            return 1;
        }
    }

    private static int executeList(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var world = source.getWorld();

        var cityManager = ((IServerWorldExt) world).getCityManager();
        source.sendFeedback(Text.of("%d cities found:".formatted(cityManager.getCities().size())), false);
        for (var city : cityManager.getCities()) {
            source.sendFeedback(Text.of("- %s".formatted(city.getName())), false);
        }
        return 0;
    }

    enum CityGetType {
        HERE, AT, NAMED
    }
}
