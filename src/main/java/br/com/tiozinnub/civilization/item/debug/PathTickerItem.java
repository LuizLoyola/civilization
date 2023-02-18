package br.com.tiozinnub.civilization.item.debug;

import br.com.tiozinnub.civilization.entity.PathingEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PathTickerItem extends Item {
    public PathTickerItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.world.isClient()) return ActionResult.SUCCESS;

        if (!(entity instanceof PathingEntity pathingEntity)) return ActionResult.SUCCESS;

        if (user.isSneaking()) {
            user.sendMessage(Text.of(pathingEntity.togglePathfinderTicker()), false);
        } else {
            if (!pathingEntity.isPathfinderAutoTicking)
                pathingEntity.tickPathfinder();
            else
                user.sendMessage(Text.of("Can't tick pathfinder while auto ticking is enabled"), false);
        }

        return ActionResult.SUCCESS;
    }
}
