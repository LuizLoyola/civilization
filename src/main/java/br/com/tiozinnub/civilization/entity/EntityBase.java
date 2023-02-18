package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.core.ai.pathfinder.PathfinderService;
import br.com.tiozinnub.civilization.core.ai.pathfinder.WalkPace;
import br.com.tiozinnub.civilization.core.ai.pathfinder.WorldNodeViewer;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.yawBetween;
import static software.bernie.geckolib.constant.DefaultAnimations.*;

public class EntityBase extends MerchantEntity implements GeoEntity {
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private final PathfinderService pathfinderService;
    private final MovementControl movementControl;
    public boolean isPathfinderAutoTicking = true; // by default, pathfinder will tick automatically
    public boolean isPathfinderSlowTicking = false;
    private boolean debug = true;
    private WalkPace pace;

    protected EntityBase(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);

        if (world instanceof ServerWorld serverWorld) {
            this.pathfinderService = new PathfinderService(new WorldNodeViewer(serverWorld, this));
            this.movementControl = new MovementControl();
        } else {
            // pathfinder is only available on server
            this.pathfinderService = null;
            this.movementControl = null;
        }
    }

    public static DefaultAttributeContainer.Builder createEntityBaseAttributes() {
        return createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.375);
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
        controllers.add(
                new AnimationController<>(this, "Walk/Run/Idle", 5, state -> {
                    if (state.isMoving()) {
                        return state.setAndContinue(isSprinting() ? RUN : WALK);
                    } else {
                        return state.setAndContinue(IDLE);
                    }
                })
        );
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
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

        this.updateMolangVariables();

        if (isClient()) return;

        var pfServ = this.getPathfinderService();

        if (pfServ.isFindingPath()) {
            if (this.isPathfinderAutoTicking) {
                if (this.isPathfinderSlowTicking) {
                    if (this.getWorld().getTime() % 5 == 0) {
                        pfServ.tick();
                    }
                } else {
                    pfServ.tickUntilFind();
                }
            }

//            if (this.debug && pfServ.pathfinder != null) {
//                for (var node : pfServ.pathfinder.nodes) {
//                    var isOpen = pfServ.pathfinder.isOpen(node.index());
//                    var parentPos = (node.parentIndex() == -1 ? this.getBlockPos() : pfServ.pathfinder.nodes.get(node.parentIndex()).pos()).toCenterPos();
//                    var pos = node.pos().toCenterPos();
//                    var particleType = ParticleTypes.FLAME;
//                    if (isOpen) {
//                        particleType = ParticleTypes.SOUL_FIRE_FLAME;
//                        pos = pos.add(0, .5f, 0);
//                    }
//                    ParticleHelper.drawParticleLine(getWorld(), parentPos, pos, particleType, 3d, 5);
//                }
//            }
        }

        var path = pfServ.getPathAndClear();
        if (path != null) {
            this.getMovementControl().followPath(path);
        }

//        path = this.getMovementControl().path;

        if (this.debug && path != null) {
            // debug
            var prevPos = this.getBlockPos();
//            for (var node : this.getMovementControl().getRemainingSteps()) {
//                var pos = node.pos();
//                ParticleHelper.drawParticleLine(getWorld(), prevPos.toCenterPos(), pos.toCenterPos(), ParticleTypes.CRIT, 3d, 2);
//                prevPos = pos;
//            }
        }

        this.getMovementControl().tick();
    }

    private void updateMolangVariables() {

    }

    public void setMovementTarget(BlockPos pos, WalkPace pace) {
        this.getMovementControl().stopMove();
        this.pace = pace;
        this.getPathfinderService().startPathfinder(this.getBlockPos(), pos);

//        this.getMovementControl().walkTo(pos, pace, true);

//        var closestItem = getWorld().getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(5), item -> true).stream().findFirst().orElse(null);
//        this.getMovementControl().walkTo(pos, pace, false).anchorLook(closestItem, false);

//        var closestPlayer = getWorld().getClosestPlayer(this, 5);
//        this.getMovementControl().walkTo(pos, pace, false).anchorLook(closestPlayer, false);

//        this.getMovementControl().followPath(path);
    }

    private PathfinderService getPathfinderService() {
        if (this.isClient()) throw new IllegalStateException("PathfinderService is only available on server");
        return this.pathfinderService;
    }

    private MovementControl getMovementControl() {
        if (this.isClient()) throw new IllegalStateException("MovementControl is only available on server");
        return this.movementControl;
    }

    public String togglePathfinderTicker() {
        if (this.isPathfinderAutoTicking) {
            this.isPathfinderAutoTicking = false;
            this.isPathfinderSlowTicking = true;
            return "Pathfinder is now ticking slowly";
        } else if (this.isPathfinderSlowTicking) {
            this.isPathfinderSlowTicking = false;
            return "Pathfinder is now ticking manually";
        } else {
            this.isPathfinderAutoTicking = true;
            return "Pathfinder is now ticking automatically";
        }
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
    }

    private class MovementControl extends Serializable {
        private BlockPos targetBlock;
        private WalkPace pace;
        private long startTime;
        private int walkTicks;
        private Vec3d targetPos;
        private boolean resetLookWhenStop;
        private Vec3d lookPos;
        private UUID lookEntityId;
        private boolean isWalking;
        private boolean hasLookAnchor;

        @Override
        public void registerProperties(SerializableHelper helper) {
            helper.registerProperty("targetBlock", () -> this.targetBlock, (value) -> this.targetBlock = value, null);
            helper.registerProperty("pace", () -> this.pace.asString(), (value) -> this.pace = WalkPace.fromString(value), null);
            helper.registerProperty("startTime", () -> this.startTime, (value) -> this.startTime = value, 0);
            helper.registerProperty("walkTicks", () -> this.walkTicks, (value) -> this.walkTicks = value, 0);
            helper.registerProperty("targetPos", () -> this.targetPos, (value) -> this.targetPos = value, null);
            helper.registerProperty("resetLookWhenStop", () -> this.resetLookWhenStop, (value) -> this.resetLookWhenStop = value, false);
            helper.registerProperty("lookPos", () -> this.lookPos, (value) -> this.lookPos = value, null);
            helper.registerProperty("lookEntityId", () -> this.lookEntityId, (value) -> this.lookEntityId = value, null);
            helper.registerProperty("isWalking", () -> this.isWalking, (value) -> this.isWalking = value, false);
            helper.registerProperty("hasLookAnchor", () -> this.hasLookAnchor, (value) -> this.hasLookAnchor = value, false);
        }

        public MovementControl walkTo(BlockPos pos, WalkPace pace, boolean resetLook) {
            this.stopMove();
            this.isWalking = true;
            this.targetBlock = pos;
            this.targetPos = Vec3d.ofBottomCenter(pos);
            this.pace = pace;
            this.startTime = getWorld().getTime();

            if (resetLook) {
                this.stopLook();
            }

            return this;
        }

        public MovementControl anchorLook(Vec3d lookTarget, boolean resetWhenDone) {
            this.hasLookAnchor = lookTarget != null;
            this.lookPos = lookTarget;
            this.resetLookWhenStop = lookTarget != null && resetWhenDone;

            return this;
        }

        public MovementControl anchorLook(Entity lookTarget, boolean resetWhenDone) {
            this.hasLookAnchor = lookTarget != null;
            this.setLookEntity(lookTarget);
            this.resetLookWhenStop = lookTarget != null && resetWhenDone;

            return this;
        }

        private void reset() {
            this.resetLookWhenStop = true;
            this.stopMove();
            this.startTime = -1;
            this.walkTicks = 0;
        }

        private double getDistanceToTarget() {
            return this.targetPos.distanceTo(getPos());
        }

        public void tick() {
            this.tickLook();
            this.tickMove();
        }

        private void tickLook() {
            if (this.hasLookAnchor) {
                if (this.lookEntityId != null) {
                    var lookEntity = getLookEntity();
                    if (lookEntity == null) {
                        this.stopLook();
                    } else {
                        this.lookPos = lookEntity.getEyePos();
                    }
                }

                if (this.lookPos != null) {
                    this.lookAt(this.lookPos, true);
                    return;
                }
            }

            if (this.isWalking) {
                // look at walk target

                // look a bit down unless we are close to the target
                var yOffset = getDistanceToTarget() < 1d ? 0d : -.5d;

                this.lookPos = this.targetPos.add(0, yOffset, 0);

                this.lookAt(this.lookPos, false);
            }
        }

        private void tickMove() {
            if (this.targetBlock == null) return;
            this.walkTicks++;

            this.strafeTo(this.targetPos);

            if (getDistanceToTarget() < 0.25d) {
                this.stopMove();
            }
        }

        private void strafeTo(Vec3d targetPos) {
            var yaw = yawBetween(getPos(), this.lookPos);
            var yawToTarget = yawBetween(getPos(), targetPos);

            var yawDiff = yaw - yawToTarget;

            var forwardsSpeed = (float) Math.cos(yawDiff);
            var sidewaysSpeed = (float) -Math.sin(yawDiff);

            getMoveControl().strafeTo(forwardsSpeed, sidewaysSpeed);
        }

        private void stopStrafe() {
            getMoveControl().strafeTo(0, 0);
        }

        private void lookAt(Vec3d target, boolean isExact) {
            EntityBase.this.lookAt(isExact ? EntityAnchorArgumentType.EntityAnchor.EYES : EntityAnchorArgumentType.EntityAnchor.FEET, target);
        }

        public MovementControl stopMove() {
            this.stopStrafe();
            this.isWalking = false;
            this.targetPos = null;
            this.targetBlock = null;
            this.pace = WalkPace.DEFAULT;

            if (this.resetLookWhenStop) {
                this.stopLook();
            }

            return this;
        }

        public MovementControl stopLook() {
            this.lookPos = null;
            this.setLookEntity(null);
            this.resetLookWhenStop = false;
            this.hasLookAnchor = false;

            return this;
        }

        public Entity getLookEntity() {
            return lookEntityId == null ? null : this.getWorld().getEntity(lookEntityId);
        }

        public void setLookEntity(Entity lookEntity) {
            this.setLookEntityId(lookEntity == null ? null : lookEntity.getUuid());
        }

        private ServerWorld getWorld() {
            return (ServerWorld) EntityBase.this.getWorld();
        }

        public void setLookEntityId(UUID lookEntityId) {
            this.lookEntityId = lookEntityId;
        }
    }
}
