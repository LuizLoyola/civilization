package br.com.tiozinnub.civilization.block.building;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class CityBuildingBlockEntity<D extends CityBuildingBlockData> extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private D cityBuildingData;

    public CityBuildingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract D initializeBuildingData(Random random);

    protected abstract D initializeBuildingData(NbtCompound nbt);

    @Override
    public void readNbt(NbtCompound nbt) {
        var compound = nbt.getCompound("cityBuildingData");
        if (compound != null) {
            if (cityBuildingData == null) cityBuildingData = initializeBuildingData(compound);
            else cityBuildingData.fromNbt(compound);
        }
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        getData().toNbt(nbt, "cityBuildingData");
    }

    protected D getData() {
        if (cityBuildingData == null) {
            cityBuildingData = this.initializeBuildingData(getWorld().getRandom());
            this.markDirty();
        }

        return cityBuildingData;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
//        controllerRegistrar.add(new AnimationController<>(this, state -> state.setAndContinue(DefaultAnimations.IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.SUCCESS;
    }
}
