package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.core.ai.pathfinder.Path;
import br.com.tiozinnub.civilization.core.ai.pathfinder.PathfinderService;
import br.com.tiozinnub.civilization.core.ai.pathfinder.Step;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Collections;
import java.util.List;

public class EntityBase extends MerchantEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PathfinderService pathfinderService;
    private final MovementControl movementControl;
    public boolean isPathfinderAutoTicking = true; // by default, pathfinder will tick automatically
    public boolean isPathfinderSlowTicking = false;


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
            if (this.isPathfinderAutoTicking) {
                if (this.isPathfinderSlowTicking) {
                    if (this.getWorld().getTime() % 5 == 0) {
                        pfServ.tick();
                    }
                } else {
                    pfServ.tickUntilFind();
                }
            }

            // debug
            if (pfServ.pathfinder != null) {
                for (var node : pfServ.pathfinder.nodes) {
                    var isOpen = pfServ.pathfinder.isOpen(node.index());
                    var parentPos = (node.parentIndex() == -1 ? this.getBlockPos() : pfServ.pathfinder.nodes.get(node.parentIndex()).pos()).toCenterPos();
                    var pos = node.pos().toCenterPos();
                    var particleType = ParticleTypes.FLAME;
                    if (isOpen) {
                        particleType = ParticleTypes.SOUL_FIRE_FLAME;
                        pos = pos.add(0, .5f, 0);
                    }
                    ParticleHelper.drawParticleLine(getWorld(), parentPos, pos, particleType, 3d, 5);
                }
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
            for (var node : this.getMovementControl().getRemainingSteps()) {
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
//        this.move(MovementType.PLAYER, new Vec3d(0, 1, 0));
//        this.jump();
        this.getMoveControl().moveTo(10, 2, 0, 1);
    }

    private class MovementControl extends Serializable {
        private Path path;
        private int stepIndex = 0;

        private Step getStep() {
            return path.getSteps().get(stepIndex);
        }

        @Override
        public void registerProperties(SerializableHelper helper) {
            helper.registerProperty("path", this::getPath, this::setPath, Path::new);
            helper.registerProperty("stepIndex", () -> this.stepIndex, i -> this.stepIndex = i, 0);
        }

        public void tick() {
            if (this.path == null) return;

            // is close enough to current step
            var distanceToStep = this.getDistanceToStep();

            if (distanceToStep < 0.5) {
                this.stepIndex++;
                if (this.stepIndex >= path.getSteps().size()) {
                    this.path = null;
                    return;
                }
            }

            var stepPos = getStepPos();
            getMoveControl().moveTo(stepPos.x, stepPos.y, stepPos.z, 0.5f);
        }

        private double getDistanceToStep() {
            return getPos().distanceTo(getStepPos());
        }

        private Vec3d getStepPos() {
            var step = this.getStep();
            return step.pos().toCenterPos().add(0, -.5f, 0);
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

        public List<Step> getRemainingSteps() {
            if (this.path == null) return Collections.emptyList();
            return this.path.getSteps().subList(this.stepIndex, this.path.getSteps().size());
        }
    }
}
