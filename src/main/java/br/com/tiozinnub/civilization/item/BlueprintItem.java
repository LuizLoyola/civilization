package br.com.tiozinnub.civilization.item;

import br.com.tiozinnub.civilization.core.blueprinting.BlueprintMaker;
import br.com.tiozinnub.civilization.registry.ItemGroupRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.math.Box;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;
import static br.com.tiozinnub.civilization.utils.helper.ParticleHelper.drawParticleBox;

public class BlueprintItem extends ItemBase {
    public BlueprintItem() {
        super(new FabricItemSettings()
                .group(ItemGroupRegistry.SPECIAL_ITEMS)
                .maxCount(1)
                .fireproof()
                .rarity(Rarity.EPIC)
        );
    }

    @Override
    public Identifier getIdentifier() {
        return idFor("blueprint");
    }

    @Override
    protected ActionResult useOnBlockServer(ItemUsageContext context) {
        var player = context.getPlayer();
        if (player == null) return ActionResult.PASS;

        var blueprintMaker = new BlueprintMaker();
        blueprintMaker.fromNbt(context.getStack().getNbt());
        player.sendMessage(blueprintMaker.usedOnBlock(context.getWorld(), context.getBlockPos(), context.getSide()), false);
        context.getStack().setNbt(blueprintMaker.toNbt());
        return ActionResult.CONSUME;
    }

    @Override
    protected TypedActionResult<ItemStack> useServer(ServerWorld world, PlayerEntity player, Hand hand) {
        var blueprintMaker = new BlueprintMaker();
        var stack = player.getStackInHand(hand);
        blueprintMaker.fromNbt(stack.getNbt());
        player.sendMessage(blueprintMaker.usedOnAir(world, player.isSneaking()), false);
        stack.setNbt(blueprintMaker.toNbt());
        return TypedActionResult.success(stack, true);
    }

    @Override
    protected void inventoryTickClient(ItemStack stack, ClientWorld world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!PlayerInventory.isValidHotbarIndex(slot)) return;

        var blueprintMaker = new BlueprintMaker();
        blueprintMaker.fromNbt(stack.getNbt());

        if (blueprintMaker.firstPos != null && blueprintMaker.secondPos != null) {
            drawParticleBox(world, new Box(blueprintMaker.firstPos, blueprintMaker.secondPos), ParticleTypes.FLAME);
        } else if (blueprintMaker.firstPos != null) {
            drawParticleBox(world, blueprintMaker.firstPos, ParticleTypes.FLAME);
        }
    }
}
