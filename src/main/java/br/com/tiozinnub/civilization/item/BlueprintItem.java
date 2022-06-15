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
    protected ActionResult useOnBlockServer(ServerWorld world, ItemUsageContext context) {
        var player = context.getPlayer();
        if (player == null) return ActionResult.PASS;

        var blueprintMaker = new BlueprintMaker(world);
        blueprintMaker.fromNbt(context.getStack().getNbt());
        var msg = blueprintMaker.usedOnBlock(world, context.getBlockPos(), context.getSide(), player.isSneaking());
        if (msg != null) player.sendMessage(msg, false);
        context.getStack().setNbt(blueprintMaker.toNbt());
        return ActionResult.CONSUME;
    }

    @Override
    protected TypedActionResult<ItemStack> useServer(ServerWorld world, PlayerEntity player, Hand hand) {
        var blueprintMaker = new BlueprintMaker(world);
        var stack = player.getStackInHand(hand);
        blueprintMaker.fromNbt(stack.getNbt());
        var msg = blueprintMaker.usedOnAir(world, player.isSneaking());
        if (msg != null) player.sendMessage(msg, false);
        stack.setNbt(blueprintMaker.toNbt());
        return TypedActionResult.success(stack, true);
    }

    @Override
    protected void inventoryTickClient(ItemStack stack, ClientWorld world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!PlayerInventory.isValidHotbarIndex(slot)) return;

        var blueprintMaker = new BlueprintMaker(null);
        blueprintMaker.fromNbt(stack.getNbt());

        var box = blueprintMaker.getBox();
        if (box != null) {
            var direction = blueprintMaker.getDirection();
            if (direction != null)
                drawParticleBox(world, box, ParticleTypes.FLAME, direction.asDirection(), ParticleTypes.SOUL_FIRE_FLAME);
            else
                drawParticleBox(world, box, ParticleTypes.FLAME);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        var blueprintMaker = new BlueprintMaker(null);
        blueprintMaker.fromNbt(stack.getNbt());

        return blueprintMaker.loadMode;
    }
}
