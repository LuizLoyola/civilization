package br.com.tiozinnub.civilization.item.debug;

import br.com.tiozinnub.civilization.entity.EntityBase;
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

        if (!(entity instanceof EntityBase entityBase)) return ActionResult.SUCCESS;

        if (user.isSneaking()) {
            entityBase.togglePathfinderTicker();
            user.sendMessage(Text.of("Pathfinder is now " + (entityBase.isPathfinderAutoTicking ? "auto ticking" : "manual ticking")), false);
        } else {
            if (!entityBase.isPathfinderAutoTicking)
                entityBase.tickPathfinder();
            else
                user.sendMessage(Text.of("Pathfinder is auto ticking"), false);
        }

        return ActionResult.SUCCESS;
    }
}
