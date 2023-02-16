package br.com.tiozinnub.civilization.item;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public abstract class ItemWithData<D extends Serializable> extends Item {
    public ItemWithData(Settings settings) {
        super(settings);
    }

    private D getData(ItemStack stack) {
        if (stack.hasNbt()) {
            return dataFromNbt(stack.getNbt());
        } else {
            return dataFromNbt(new NbtCompound());
        }
    }

    protected abstract D dataFromNbt(NbtCompound nbt);


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        var result = useOnEntity(stack, getData(stack), user, entity, hand);
        stack.setNbt(getData(stack).toNbt());
        return result;
    }

    public ActionResult useOnEntity(ItemStack stack, D data, PlayerEntity user, LivingEntity entity, Hand hand) {
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var result = useOnBlock(context, getData(context.getStack()));
        context.getStack().setNbt(getData(context.getStack()).toNbt());
        return result;
    }

    public ActionResult useOnBlock(ItemUsageContext context, D data) {
        return super.useOnBlock(context);
    }
}
