package br.com.tiozinnub.civilization.item;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class ItemBase extends Item {
    public ItemBase(Item.Settings settings) {
        super(settings);
    }

    public abstract Identifier getIdentifier();

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return context.getWorld().isClient() ? useOnBlockClient((ClientWorld) context.getWorld(), context) : useOnBlockServer((ServerWorld) context.getWorld(), context);
    }

    protected ActionResult useOnBlockClient(ClientWorld world, ItemUsageContext context) {
        return super.useOnBlock(context);
    }

    protected ActionResult useOnBlockServer(ServerWorld world, ItemUsageContext context) {
        return super.useOnBlock(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) {
            inventoryTickClient(stack, (ClientWorld) world, entity, slot, selected);
        } else {
            inventoryTickServer(stack, (ServerWorld) world, entity, slot, selected);
        }
    }

    protected void inventoryTickClient(ItemStack stack, ClientWorld world, Entity entity, int slot, boolean selected) {
    }

    protected void inventoryTickServer(ItemStack stack, ServerWorld world, Entity entity, int slot, boolean selected) {
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            return useClient((ClientWorld) world, user, hand);
        } else {
            return useServer((ServerWorld) world, user, hand);
        }
    }

    protected TypedActionResult<ItemStack> useClient(ClientWorld world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand);
    }

    protected TypedActionResult<ItemStack> useServer(ServerWorld world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand);
    }
}
