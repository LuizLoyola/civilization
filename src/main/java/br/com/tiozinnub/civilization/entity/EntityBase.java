package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.core.ai.pathfinder.Path;
import br.com.tiozinnub.civilization.core.ai.pathfinder.PathfinderService;
import br.com.tiozinnub.civilization.core.ai.pathfinder.WorldNodeViewer;
import br.com.tiozinnub.civilization.utils.Serializable;
import br.com.tiozinnub.civilization.utils.helper.ParticleHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EntityBase extends MerchantEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PathfinderService pathfinderService;
    private final MovementControl movementControl;
    public boolean isPathfinderAutoTicking = true; // by default, pathfinder will tick automatically


    //    private double pathfinderTickMultiplier = 1f / 5;
    private double pathfinderTickMultiplier = 5000;


    protected EntityBase(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);

        if (world instanceof ServerWorld serverWorld) {
            this.pathfinderService = new PathfinderService(new WorldNodeViewer(serverWorld));
            this.movementControl = new MovementControl();
        } else {
            // pathfinder is only available on server
            this.pathfinderService = null;
            this.movementControl = null;
        }
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, state -> state.setAndContinue(DefaultAnimations.IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        return false;
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
    }

    @Override
    protected void fillRecipes() {
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (isClient()) return;

        var pfServ = this.getPathfinderService();

        if (pfServ.isFindingPath()) {
            if (this.isPathfinderAutoTicking)
                pfServ.tickWarp(this.pathfinderTickMultiplier);

            // debug
            for (var node : pfServ.pathfinder.nodes) {
                var isOpen = pfServ.pathfinder.isOpen(node.index());
                if (isOpen) continue;
                var parentPos = node.parentIndex() == -1 ? this.getBlockPos() : pfServ.pathfinder.nodes.get(node.parentIndex()).pos();
                ParticleHelper.drawParticleLine(getWorld(), node.pos().toCenterPos(), parentPos.toCenterPos(), ParticleTypes.FLAME, 3d, 5);
            }
        }

        var path = pfServ.getPathAndClear();
        if (path != null) {
            this.getMovementControl().setPath(path);
        }

        path = this.getMovementControl().path;

        if (path != null) {
            // debug
            var prevPos = this.getBlockPos();
            for (var node : this.getMovementControl().path.getSteps()) {
                var pos = node.pos();
                ParticleHelper.drawParticleLine(getWorld(), prevPos.toCenterPos(), pos.toCenterPos(), ParticleTypes.CRIT, 3d, 2);
                prevPos = pos;
            }
        }

        this.getMovementControl().tick();
    }

    public void setMovementTarget(BlockPos pos) {
        this.getMovementControl().clear();
        this.getPathfinderService().startPathfinder(this.getBlockPos(), pos);
    }

    private PathfinderService getPathfinderService() {
        if (this.isClient()) throw new IllegalStateException("PathfinderService is only available on server");
        return this.pathfinderService;
    }

    private MovementControl getMovementControl() {
        if (this.isClient()) throw new IllegalStateException("MovementControl is only available on server");
        return this.movementControl;
    }

    public void togglePathfinderTicker() {
        this.isPathfinderAutoTicking = !this.isPathfinderAutoTicking;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        assert this.movementControl != null;
        this.movementControl.fromNbt(nbt, "movementControl");

        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        assert this.movementControl != null;
        this.movementControl.toNbt(nbt, "movementControl");

        super.writeCustomDataToNbt(nbt);
    }

    public void tickPathfinder() {
        this.getPathfinderService().tick();
//        this.move(MovementType.PLAYER, new Vec3d(0, 1, 0));
//        this.jump();
    }

    private class MovementControl extends Serializable {
        private Path path;
        private int stepIndex = 0;

        @Override
        public void registerProperties(SerializableHelper helper) {
            helper.registerProperty("path", this::getPath, this::setPath, Path::new);
            helper.registerProperty("stepIndex", () -> this.stepIndex, i -> this.stepIndex = i, 0);
        }

        public void tick() {

        }

        public void clear() {
            this.path = null;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
            this.stepIndex = 0;
        }
    }
}
