package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.core.ai.movement.pathing.Path;
import br.com.tiozinnub.civilization.core.ai.movement.pathing.PathfinderService;
import br.com.tiozinnub.civilization.core.ai.movement.pathing.WorldNodeViewer;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public abstract class PathingEntity extends EntityBase {
    private final PathfinderService pathfinderService;
    private final PathFollower pathFollower;
    public boolean isPathfinderAutoTicking = true; // by default, pathfinder will tick automatically
    public boolean isPathfinderSlowTicking = false;
    private boolean debug = true;

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
            this.getPathFollower().followPath(path);
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


    }

    private class PathFollower extends Serializable {
        private Path path;

        @Override
        public void registerProperties(SerializableHelper helper) {

        }

        public void followPath(Path path) {
            this.path = path;
        }

        public void tick() {
            if (this.path == null) return;

            
        }
    }
}
