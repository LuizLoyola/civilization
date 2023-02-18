package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.core.ai.pathfinder.PathfinderService;
import br.com.tiozinnub.civilization.core.ai.pathfinder.WalkPace;
import br.com.tiozinnub.civilization.core.ai.pathfinder.WorldNodeViewer;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

import static software.bernie.geckolib.constant.DefaultAnimations.RUN;
import static software.bernie.geckolib.constant.DefaultAnimations.WALK;

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
                    return state.setAndContinue(isSprinting() ? RUN : WALK);
//                    if (state.isMoving()) {
//                    } else {
//                        return state.setAndContinue(IDLE);
//                    }
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
//            this.getMovementControl().setPath(path);
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

        var closestItem = getWorld().getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(5), item -> true).stream().findFirst().orElse(null);

//        this.getMovementControl().walkTo(pos, pace, true);
        this.getMovementControl().walkTo(pos, pace, false).anchorLook(closestItem, false);
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

        @Override
        public void registerProperties(SerializableHelper helper) {

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
            this.lookPos = lookTarget;
            this.resetLookWhenStop = resetWhenDone;

            return this;
        }

        public MovementControl anchorLook(Entity lookTarget, boolean resetWhenDone) {
            this.setLookEntity(lookTarget);
            this.resetLookWhenStop = resetWhenDone;

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

        private boolean getHasLookAnchor() {
            return this.lookPos != null || this.lookEntityId != null;
        }

        private void tickLook() {
            if (getHasLookAnchor()) {
                Vec3d pos;
                if (lookPos != null)
                    pos = this.lookPos;
                else {
                    var entity = this.getLookEntity();

                    if (entity == null) {
                        this.stopLook();
                        return;
                    }

                    pos = entity.getPos().add(0, entity.getEyeY(), 0);
                }

                this.lookAt(pos, true);
            } else if (this.isWalking) {
                // look at walk target

                // look a bit down unless we are close to the target
                var yOffset = getDistanceToTarget() < 1d ? 0d : -.5d;

                this.lookAt(this.targetPos.add(0, yOffset, 0), false);
            }
        }

        private void tickMove() {
            if (this.targetBlock == null) return;
            this.walkTicks++;

            if (!getHasLookAnchor()) {
                // no anchor, just walk forward
                this.walkForward();
            } else {
                // get angle and calculate how much forwards and sideways we should go
                this.strafeTo(this.targetPos);
            }

            if (getDistanceToTarget() < 0.25d) {
                this.stopMove();
            }
        }

        private void strafeTo(Vec3d targetPos) {
            // get angle we are looking at through look target and current position and throwing math at it
            var yaw = (float) Math.toDegrees(Math.atan2(targetPos.z - getPos().z, targetPos.x - getPos().x)) - 90f;

            var forwardsSpeed = 1f;

            // how much speed we should take from the forwards speed and put into the sideways speed depending on the angle?
            var sidewaysSpeed = Math.abs(MathHelper.wrapDegrees(yaw - getHeadYaw())) / 90f;

            forwardsSpeed *= 1f - sidewaysSpeed;

            // if we are looking backwards, we should go backwards
            if (Math.abs(MathHelper.wrapDegrees(yaw - getHeadYaw())) > 90f) {
                forwardsSpeed *= -1f;
            }

            getMoveControl().strafeTo(forwardsSpeed, sidewaysSpeed);
        }

        private void walkForward() {
            getMoveControl().strafeTo(1f, 0);
        }

        private void lookAt(Vec3d target, boolean isExact) {
            EntityBase.this.lookAt(isExact ? EntityAnchorArgumentType.EntityAnchor.EYES : EntityAnchorArgumentType.EntityAnchor.FEET, target);
        }

        public MovementControl stopMove() {
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

            return this;
        }

        public Entity getLookEntity() {
            return lookEntityId == null ? null : this.getWorld().getEntity(lookEntityId);
        }

        public void setLookEntity(Entity lookEntity) {
            this.lookEntityId = lookEntity == null ? null : lookEntity.getUuid();
        }

        private ServerWorld getWorld() {
            return (ServerWorld) EntityBase.this.getWorld();
        }

        public void setLookEntityId(UUID lookEntityId) {
            this.lookEntityId = lookEntityId;
        }
    }
}
