package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.core.ai.movement.Step;
import br.com.tiozinnub.civilization.core.ai.movement.WalkPace;
import br.com.tiozinnub.civilization.core.ai.movement.pathing.Path;
import br.com.tiozinnub.civilization.core.ai.movement.pathing.PathfinderService;
import br.com.tiozinnub.civilization.core.ai.movement.pathing.WorldNodeViewer;
import br.com.tiozinnub.civilization.utils.Serializable;
import br.com.tiozinnub.civilization.utils.helper.ParticleHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public abstract class PathingEntity extends EntityBase {
    private final PathfinderService pathfinderService;
    private final PathFollower pathFollower;
    public boolean isPathfinderAutoTicking = true; // by default, pathfinder will tick automatically
    public boolean isPathfinderSlowTicking = false;
    private boolean debug = false;
    private WalkPace nextTargetPace;
    private boolean nextTargetResetLook;

    protected PathingEntity(EntityType<? extends EntityBase> entityType, World world) {
        super(entityType, world);

        if (world instanceof ServerWorld serverWorld) {
            this.pathfinderService = new PathfinderService(new WorldNodeViewer(serverWorld, this));
            this.pathFollower = new PathFollower();
        } else {
            // pathfinder is only available on server
            this.pathfinderService = null;
            this.pathFollower = null;
        }
    }

    public static DefaultAttributeContainer.Builder createPathingEntityAttributes() {
        return createEntityBaseAttributes();
    }

    public void tickPathfinder() {
        this.getPathfinderService().tick();
    }

    private PathfinderService getPathfinderService() {
        if (this.isClient()) throw new IllegalStateException("PathfinderService is only available on server");
        return this.pathfinderService;
    }

    public PathFollower getPathFollower() {
        if (this.isClient()) throw new IllegalStateException("PathFollower is only available on server");
        return this.pathFollower;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        assert this.pathFollower != null;
        this.pathFollower.fromNbt(nbt.getCompound("pathFollower"));

        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        assert this.pathFollower != null;
        this.pathFollower.toNbt(nbt, "pathFollower");

        super.writeCustomDataToNbt(nbt);
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
    public void tickMovement() {
        super.tickMovement();

        if (this.isClient()) return;

        var pfServ = this.getPathfinderService();

        if (pfServ.isFindingPath()) {
            if (this.isPathfinderSlowTicking) {
                if (this.getWorld().getTime() % 20 == 0) {
                    pfServ.tick();
                }
            } else if (this.isPathfinderAutoTicking) {
                pfServ.tickUntilFind();
            }

            if (this.debug && pfServ.pathfinder != null) {
                for (var node : pfServ.pathfinder.nodes) {
                    var isOpen = pfServ.pathfinder.isOpen(node.index());
                    var parentPos = (node.parentIndex() == -1 ? this.getPos() : pfServ.pathfinder.nodes.get(node.parentIndex()).pos());
                    var pos = node.pos().add(0d, .1d, 0d);
                    var particleType = ParticleTypes.FLAME;
                    if (isOpen) {
                        particleType = ParticleTypes.SOUL_FIRE_FLAME;
                        pos = pos.add(0d, .4d, 0d);
                    }
                    ParticleHelper.drawParticleLine(getWorld(), parentPos, pos, particleType, 3d, 5);
                }
            }
        }

        var path = pfServ.getPathAndClear();
        if (path != null) {
            this.getPathFollower().followPath(path, nextTargetPace, nextTargetResetLook);
        }

//        path = this.getMovementControl().path;

        if (this.debug && this.getPathFollower().path != null) {
            var prevPos = this.getPos().add(0, 0.1d, 0);
            List<Step> steps = this.getPathFollower().path.getSteps();
            int i = this.getPathFollower().stepIndex;
            if (i >= 0) {
                for (; i < steps.size(); i++) {
                    var node = steps.get(i);
                    var pos = node.toPos().add(0, 0.1d, 0);
                    ParticleHelper.drawParticleLine(getWorld(), prevPos, pos, ParticleTypes.CRIT, 3d, 2);
                    prevPos = pos;
                }
            }
        }

        this.getPathFollower().tick();
    }

    public void setMovementTarget(BlockPos pos, WalkPace pace, boolean resetLook) {
        this.getPathfinderService().startPathfinder(getPos(), Vec3d.ofBottomCenter(pos));
        this.getPathFollower().finishPath();
        this.nextTargetPace = pace;
        this.nextTargetResetLook = resetLook;
    }


    private class PathFollower extends Serializable {
        private Path path;
        private WalkPace pace;
        private int stepIndex;
        private Step currentStep;
        private int stepTime;

        @Override
        public void registerProperties(SerializableHelper helper) {

        }

        public void followPath(Path path, WalkPace pace, boolean resetLook) {
            this.finishPath();
            this.path = path;
            this.pace = pace;

            if (resetLook) {
                getMovementControl().stopLook();
            }
        }

        public void tick() {
            if (this.path == null) return;

            if (this.currentStep == null) {
                this.goToNextStep();
                return;
            } else {
                this.stepTime++;
            }

            var distance = getPos().distanceTo(this.currentStep.toPos());

            var isLastStep = this.stepIndex >= this.path.getSteps().size();

            if (isLastStep) {
                // let it get closer to target
                if (distance < 0.25f) {
                    this.finishPath();
                }
            } else {
                if (distance < 0.75f) {
                    this.goToNextStep();
                }
            }
        }

        private void goToNextStep() {
            this.stepIndex++;

            if (this.stepIndex >= this.path.getSteps().size()) {
                this.finishPath();
                return;
            }

            this.currentStep = this.path.getSteps().get(this.stepIndex);
            this.stepTime = 0;

            if (this.pace == WalkPace.SNEAKY) {
                setPose(EntityPose.CROUCHING);
            } else {
                setPose(EntityPose.STANDING);
            }

            getMovementControl().walkTo(this.currentStep.toPos(), this.pace, false);
        }

        private void finishPath() {
            this.path = null;
            this.currentStep = null;
            this.stepIndex = -1;
            this.stepTime = 0;
        }
    }
}
