package br.com.tiozinnub.civilization.item.debug;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import br.com.tiozinnub.civilization.item.ItemWithData;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.UUID;

public class PathWandItem extends ItemWithData<PathWandItem.PathWandItemData> {
    public PathWandItem() {
        super(
                new FabricItemSettings()
                        .maxCount(1)
        );
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PathWandItemData data, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.world.isClient()) return ActionResult.SUCCESS;

        // if user sneaking, clear target
        if (user.isSneaking()) {
            data.setTargetUuid(null);
            user.sendMessage(Text.of("Target cleared"), false);
        } else {
            data.setTargetUuid(entity.getUuid());
            user.sendMessage(Text.of("Target set to " + entity.getName().getString()), false);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context, PathWandItemData data) {
        if (context.getWorld().isClient()) return ActionResult.SUCCESS;
        var player = context.getPlayer();
        if (player == null) return ActionResult.SUCCESS;

        var targetUuid = data.getTargetUuid();

        if (targetUuid == null) {
            player.sendMessage(Text.of("No target set"), false);
            return ActionResult.SUCCESS;
        }

        var serverWorld = (ServerWorld) context.getWorld();
        var personCatalog = ((IServerWorldExt) serverWorld).getPersonCatalog();

        var entityId = personCatalog.getPersonId(data.getTargetUuid());
        var personEntity = (PersonEntity) serverWorld.getEntityById(entityId);

        if (personEntity == null) {
            player.sendMessage(Text.of("Target not found"), false);
            return ActionResult.SUCCESS;
        }

        // Set path target

        return ActionResult.SUCCESS;
    }

    @Override
    protected PathWandItemData dataFromNbt(NbtCompound nbt) {
        return new PathWandItemData(nbt);
    }

    public static class PathWandItemData extends Serializable {
        private UUID targetUuid;

        public PathWandItemData(NbtCompound nbt) {
            fromNbt(nbt);
        }

        @Override
        public void registerProperties(SerializableHelper helper) {
            helper.registerProperty("targetUuid", this::getTargetUuid, this::setTargetUuid, null);
        }

        public UUID getTargetUuid() {
            return targetUuid;
        }

        public void setTargetUuid(UUID targetUuid) {
            this.targetUuid = targetUuid;
        }
    }
}
