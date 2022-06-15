package br.com.tiozinnub.civilization.command;

import br.com.tiozinnub.civilization.core.blueprinting.BlueprintMaker;
import br.com.tiozinnub.civilization.core.structure.StructureType;
import br.com.tiozinnub.civilization.registry.ItemRegistry;
import br.com.tiozinnub.civilization.utils.helper.PositionHelper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlueprintCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register() {
        return literal("blueprint").executes(BlueprintCommand::execute)
                .then(literal("save")
                        .then(argument("structure_type", StringArgumentType.string())
                                .executes(BlueprintCommand::executeSave)
                        )
                )
                .then(literal("load")
                        .then(argument("structure_type", StringArgumentType.string())
                                .executes(BlueprintCommand::executeLoad)
                        )
                );
    }


    private static BlueprintMaker getBlueprintMaker(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.of("You must be a player to use this command."));
            return null;
        }

        var stack = player.getMainHandStack();
        if (stack.isEmpty() || !stack.isOf(ItemRegistry.BLUEPRINT)) {
            context.getSource().sendError(Text.of("You must be holding a blueprint to use this command."));
            return null;
        }

        var blueprintMaker = new BlueprintMaker(context.getSource().getWorld());
        blueprintMaker.fromNbt(stack.getNbt());

        return blueprintMaker;
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        var blueprintMaker = getBlueprintMaker(context);

        if (blueprintMaker == null) {
            return 1;
        }

        var source = context.getSource();
        var firstPos = blueprintMaker.getFirstPos();
        var secondPos = blueprintMaker.getSecondPos();
        var direction = blueprintMaker.getDirection();
        source.sendFeedback(Text.of("First pos: %s".formatted(PositionHelper.blockPosString(firstPos))), false);
        source.sendFeedback(Text.of("Second pos: %s".formatted(PositionHelper.blockPosString(secondPos))), false);
        source.sendFeedback(Text.of("Direction: %s".formatted(direction != null ? direction.asString() : "null")), false);

        if (firstPos != null && secondPos != null && direction != null) {
            source.sendFeedback(Text.of("Blueprint is complete."), false);
            source.sendFeedback(Text.of("Run %s/civilization blueprint save <structure_type>%s to save it to \"(your user folder)/Desktop/blueprints\".".formatted(Formatting.BLUE, Formatting.RESET)), false);
        }

        return 0;
    }

    private static int executeSave(CommandContext<ServerCommandSource> context) {
        var blueprintMaker = getBlueprintMaker(context);

        if (blueprintMaker == null) {
            return 1;
        }

        var structureType = StructureType.byName(StringArgumentType.getString(context, "structure_type"));
        if (structureType == null) {
            context.getSource().sendError(Text.of("Invalid structure type"));
            return 1;
        }

        var fileName = blueprintMaker.saveToFile(structureType);

        if (fileName == null) {
            context.getSource().sendError(Text.of("Could not save blueprint"));
            return 1;
        }

        context.getSource().sendFeedback(Text.of("Saved blueprint to %s".formatted(fileName)), false);

        return 0;
    }

    private static int executeLoad(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.of("You must be a player to use this command."));
            return 1;
        }

        var structureType = StructureType.byName(StringArgumentType.getString(context, "structure_type"));
        if (structureType == null) {
            context.getSource().sendError(Text.of("Invalid structure type"));
            return 1;
        }

        var blueprintMaker = new BlueprintMaker(context.getSource().getWorld());
        var stack = player.getMainHandStack();

        var giveLater = false;
        if (stack.isEmpty()) {
            giveLater = true;
        } else if (!stack.isOf(ItemRegistry.BLUEPRINT)) {
            context.getSource().sendError(Text.of("You must be holding an empty hand to use this command."));
            return 1;
        } else {
            blueprintMaker.fromNbt(stack.getNbt());
        }

        blueprintMaker.setLoadMode(structureType);

        if (giveLater) {
            stack = ItemRegistry.BLUEPRINT.getDefaultStack();
            player.setStackInHand(Hand.MAIN_HAND, stack);
        }

        stack.setNbt(blueprintMaker.toNbt());

        context.getSource().sendFeedback(Text.of("Blueprint item is now a loader for %s".formatted(structureType.asString())), false);

        return 0;
    }
}
