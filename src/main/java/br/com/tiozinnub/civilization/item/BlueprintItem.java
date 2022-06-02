package br.com.tiozinnub.civilization.item;

import br.com.tiozinnub.civilization.core.blueprinting.BlueprintMaker;
import br.com.tiozinnub.civilization.registry.ItemGroupRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;
import static br.com.tiozinnub.civilization.utils.helper.ParticleHelper.drawParticleBox;
import static br.com.tiozinnub.civilization.utils.helper.StringHelper.getStringFromBlockPos;

public class BlueprintItem extends ItemBase {
    public BlueprintItem() {
        super(new Item.Settings()
                .group(ItemGroupRegistry.SPECIAL_ITEMS)
                .maxCount(1)
        );
    }

    private static void setBlueprintMaker(ItemStack itemStack, BlueprintMaker blueprintMaker) {
        itemStack.setNbt(blueprintMaker.toNbt());
    }

    private static BlueprintMaker getBlueprintMaker(ItemStack itemStack) {
        BlueprintMaker blueprintMaker;
        if (itemStack.hasNbt()) {
            blueprintMaker = BlueprintMaker.newFromNbt(itemStack.getNbt());
        } else {
            blueprintMaker = new BlueprintMaker();
        }
        return blueprintMaker;
    }

    @Override
    public Identifier getIdentifier() {
        return idFor("blueprint");
    }

    @Override
    public void inventoryTickClient(ItemStack stack, ClientWorld world, Entity entity, int slot, boolean selected) {
        if (!PlayerInventory.isValidHotbarIndex(slot)) return;

        if (!(entity instanceof PlayerEntity)) return;

        var blueprintMaker = getBlueprintMaker(stack);

        if (blueprintMaker.startPos == null) return;
        if (blueprintMaker.endPos == null) {
            drawParticleBox(world, new Box(blueprintMaker.startPos, blueprintMaker.startPos), ParticleTypes.FLAME);
            return;
        }

        if (blueprintMaker.direction == null) {
            drawParticleBox(world, new Box(blueprintMaker.startPos, blueprintMaker.endPos), ParticleTypes.SOUL_FIRE_FLAME);
            return;
        }

        drawParticleBox(world, new Box(blueprintMaker.startPos, blueprintMaker.endPos), ParticleTypes.SOUL_FIRE_FLAME, blueprintMaker.direction, ParticleTypes.FLAME);
    }


    @Override
    public ActionResult useOnBlockServer(ItemUsageContext context) {
        var player = context.getPlayer();
        if (player == null) {
            return super.useOnBlock(context);
        }

        var itemStack = context.getStack();

        var blueprintMaker = getBlueprintMaker(itemStack);
        var blockPos = context.getBlockPos();
        var direction = context.getSide();

        var result = this.handleUseOnBlock((ServerWorld) player.getWorld(), player, itemStack, blueprintMaker, blockPos, direction);

        setBlueprintMaker(itemStack, blueprintMaker);

        return result;
    }

    private ActionResult handleUseOnBlock(ServerWorld world, PlayerEntity player, ItemStack itemStack, BlueprintMaker blueprintMaker, BlockPos blockPos, Direction direction) {
        if (player.isSneaky()) {
            blueprintMaker.startPos = null;
            blueprintMaker.endPos = null;
            blueprintMaker.mainBlockPos = null;
            blueprintMaker.stages.clear();
            return ActionResult.CONSUME;
        }

        if (blueprintMaker.startPos == null) {
            blueprintMaker.startPos = blockPos;
            player.sendMessage(Text.of("Start at %s".formatted(getStringFromBlockPos(blueprintMaker.startPos, true))), true);
            return ActionResult.CONSUME;
        }

        if (blueprintMaker.endPos == null) {
            if (blueprintMaker.startPos.equals(blockPos)) {
                blueprintMaker.startPos = null;

                player.sendMessage(Text.of("Start cleared."), true);
                return ActionResult.CONSUME;
            }

            blueprintMaker.endPos = blockPos;
            player.sendMessage(Text.of("End at %s".formatted(getStringFromBlockPos(blueprintMaker.endPos, true))), true);
            return ActionResult.CONSUME;
        }

        var box = blueprintMaker.getBox();
        assert box != null;

        if (blueprintMaker.mainBlockPos == null) {
            if (blueprintMaker.endPos.equals(blockPos)) {
                blueprintMaker.endPos = null;

                player.sendMessage(Text.of("End cleared."), true);
                return ActionResult.CONSUME;
            }

            blueprintMaker.mainBlockPos = blockPos;

            player.sendMessage(Text.of("Main block at %s".formatted(getStringFromBlockPos(blueprintMaker.mainBlockPos, true))), true);
            return ActionResult.CONSUME;
        }

        if (blueprintMaker.mainBlockPos.equals(blockPos)) {
            blueprintMaker.mainBlockPos = null;

            player.sendMessage(Text.of("Main block cleared."), true);
            return ActionResult.CONSUME;
        }

        var stage = new BlueprintMaker.Stage(world, blueprintMaker.getBox(), blueprintMaker.getMinPos());

        if (blueprintMaker.stages.size() == 0 || !stage.isSame(blueprintMaker.stages.get(blueprintMaker.stages.size() - 1))) {
            blueprintMaker.stages.add(stage);

            player.sendMessage(Text.of("Stage %d saved".formatted(blueprintMaker.stages.size())), true);
            return ActionResult.CONSUME;
        }

        // finish
        player.sendMessage(Text.of(blueprintMaker.generateBlueprintText()), false);
        return ActionResult.CONSUME;
    }


}
