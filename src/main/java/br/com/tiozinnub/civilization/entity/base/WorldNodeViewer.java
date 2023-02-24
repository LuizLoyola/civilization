package br.com.tiozinnub.civilization.entity.base;

import br.com.tiozinnub.civilization.utils.helper.PositionHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.*;

public class WorldNodeViewer extends PathfinderService.NodeViewer {
    private final ServerWorld world;
    private final PathingEntity entity;

    public WorldNodeViewer(ServerWorld world, PathingEntity entity) {
        this.world = world;
        this.entity = entity;
    }

    private static void checkLDiagonals(Vec3d pos, NodeFinder n) {
        // L DIAGONALS + diagonal

        var nne = pos.add(east()).add(north(2));
        nne = n.add(n.checkDiagonalL(pos, nne, pos.add(north()), pos.add(north()).add(east()), pos.add(north(2)), pos.add(east()), true));
        if (nne != null) n.add(n.checkDiagonal(pos, nne.add(east()).add(north()), nne.add(north()), nne.add(east())));

        var nee = pos.add(east(2)).add(north());
        nee = n.add(n.checkDiagonalL(pos, nee, pos.add(east()).add(north()), pos.add(east()), pos.add(north()), pos.add(east(2)), false));
        if (nee != null) n.add(n.checkDiagonal(pos, nee.add(east()).add(north()), nee.add(north()), nee.add(east())));

        var see = pos.add(east(2)).add(south());
        see = n.add(n.checkDiagonalL(pos, see, pos.add(east()), pos.add(east()).add(south()), pos.add(east(2)), pos.add(south()), true));
        if (see != null) n.add(n.checkDiagonal(pos, see.add(east()).add(south()), see.add(south()), see.add(east())));

        var sse = pos.add(east()).add(south(2));
        sse = n.add(n.checkDiagonalL(pos, sse, pos.add(south()).add(east()), pos.add(south()), pos.add(east()), pos.add(south(2)), false));
        if (sse != null) n.add(n.checkDiagonal(pos, sse.add(east()).add(south()), sse.add(south()), sse.add(east())));

        var ssw = pos.add(PositionHelper.west()).add(south(2));
        ssw = n.add(n.checkDiagonalL(pos, ssw, pos.add(south()), pos.add(south()).add(PositionHelper.west()), pos.add(south(2)), pos.add(PositionHelper.west()), true));
        if (ssw != null) n.add(n.checkDiagonal(pos, ssw.add(PositionHelper.west()).add(south()), ssw.add(south()), ssw.add(PositionHelper.west())));

        var sww = pos.add(PositionHelper.west(2)).add(south());
        sww = n.add(n.checkDiagonalL(pos, sww, pos.add(south()).add(PositionHelper.west()), pos.add(PositionHelper.west()), pos.add(south()), pos.add(PositionHelper.west(2)), false));
        if (sww != null) n.add(n.checkDiagonal(pos, sww.add(PositionHelper.west()).add(south()), sww.add(south()), sww.add(PositionHelper.west())));

        var nww = pos.add(PositionHelper.west(2)).add(north());
        nww = n.add(n.checkDiagonalL(pos, nww, pos.add(PositionHelper.west()), pos.add(PositionHelper.west()).add(north()), pos.add(PositionHelper.west(2)), pos.add(north()), true));
        if (nww != null) n.add(n.checkDiagonal(pos, nww.add(PositionHelper.west()).add(north()), nww.add(north()), nww.add(PositionHelper.west())));

        var nnw = pos.add(PositionHelper.west()).add(north(2));
        nnw = n.add(n.checkDiagonalL(pos, nnw, pos.add(north()).add(PositionHelper.west()), pos.add(north()), pos.add(PositionHelper.west()), pos.add(north(2)), false));
        if (nnw != null) n.add(n.checkDiagonal(pos, nnw.add(PositionHelper.west()).add(north()), nnw.add(north()), nnw.add(PositionHelper.west())));
    }

    private static void checkPerfectDiagonals(Vec3d pos, int maxWalkDist, NodeFinder n) {
        // PERFECT DIAGONALS
        Vec3d northEast = pos;
        Vec3d southEast = pos;
        Vec3d southWest = pos;
        Vec3d northWest = pos;

        for (int i = 1; i <= maxWalkDist; i++) {
            var ne = pos.add(east(i)).add(north(i));
            northEast = northEast == null ? null : n.add(n.checkDiagonal(pos, ne, ne.add(east(-1)), ne.add(north(-1))));
            var se = pos.add(south(i)).add(east(i));
            southEast = southEast == null ? null : n.add(n.checkDiagonal(pos, se, se.add(south(-1)), se.add(east(-1))));
            var sw = pos.add(west(i)).add(south(i));
            southWest = southWest == null ? null : n.add(n.checkDiagonal(pos, sw, sw.add(west(-1)), sw.add(south(-1))));
            var nw = pos.add(north(i)).add(west(i));
            northWest = northWest == null ? null : n.add(n.checkDiagonal(pos, nw, nw.add(north(-1)), nw.add(west(-1))));
        }
    }

    private static void checkCardinals(Vec3d pos, int maxWalkDist, NodeFinder n) {
        // CARDINALS
        Vec3d north = pos;
        Vec3d east = pos;
        Vec3d south = pos;
        Vec3d west = pos;

        for (int i = 1; i <= maxWalkDist; i++) {
            north = north == null ? null : n.add(n.checkCardinal(pos, north.add(north())));
            east = east == null ? null : n.add(n.checkCardinal(pos, east.add(east())));
            south = south == null ? null : n.add(n.checkCardinal(pos, south.add(south())));
            west = west == null ? null : n.add(n.checkCardinal(pos, west.add(PositionHelper.west())));
        }
    }

    @Override
    public List<Step> getNeighbors(Vec3d pos) {
        // if is on air, go down until find a block

        var refPos = Vec3d.ofBottomCenter(new BlockPos(pos));

        var p = refPos;
        while (isAir(p)) {
            p = p.add(PositionHelper.down());

            // somehow, the block is on the void
            if (p.getY() < world.getBottomY()) {
                p = refPos;
                break;
            }
        }

        int maxWalkDist = 3;

        var n = new NodeFinder(p.add(PositionHelper.up()));

        checkLDiagonals(refPos, n);
        checkPerfectDiagonals(refPos, maxWalkDist, n);
        checkCardinals(refPos, maxWalkDist, n);

        return n.nodeList;
    }

    private boolean isAir(Vec3d pos) {
        var blockPos = new BlockPos(pos);
        if (world.isAir(blockPos)) return true;

        var blockState = world.getBlockState(blockPos);
        var voxelShape = blockState.getCollisionShape(world, blockPos);

        return voxelShape.isEmpty();
    }

    private class NodeFinder {
        private final ArrayList<Step> nodeList = new ArrayList<>();
        private final Vec3d origin;

        public NodeFinder(Vec3d origin) {
            this.origin = origin;
        }

        public Vec3d add(Step step) {
            if (step == null) return null;
            nodeList.add(step);
            // if it is not WALK, then return null to avoid trying to walk right after jump/fall
            return step.yDiff() == 0 ? step.toPos() : null;
        }

        public Step checkCardinal(Vec3d from, Vec3d pos) {
            if (canStandOn(pos)) return new Step(from, pos);

            // can jump from origin block to toPos.up?
            if (canStandOn(origin, 1) && canStandOn(pos.add(PositionHelper.up()))) {
                if (canJumpOver(pos))
                    return new Step(from, pos.add(PositionHelper.up()));
            }

            // can fall from origin block to toPos.down?
            if (canStandOn(origin) && canStandOn(pos.add(PositionHelper.down()), 1)) return new Step(from, pos.add(PositionHelper.down()));

            return null;
        }

        public boolean canJumpOver(Vec3d pos) {
            var topSolidHeight = getSolidTopHeight(pos);

            return topSolidHeight != -1 && topSolidHeight <= 1d;
        }

        private boolean canStandOn(Vec3d target) {
            return canStandOn(target, 0);
        }

        private boolean canStandOn(Vec3d target, int extraClearance) {
            if (!isTopSolid(target.add(PositionHelper.down()))) return false;
            return isEnoughClearance(target, extraClearance);
        }

        private boolean isEnoughClearance(Vec3d target) {
            return isEnoughClearance(target, 0);
        }

        private boolean isEnoughClearance(Vec3d target, int extraClearance) {
            for (int i = 0; i < entity.getHeight() + extraClearance; i++)
                if (!isAir(target.add(up(i))))
                    return isEnoughClearanceWithStep(target, extraClearance);
            return true;
        }

        private boolean isEnoughClearanceWithStep(Vec3d target, int extraClearance) {
            var topHeight = getSolidTopHeight(target.add(up()));
            if (topHeight == -1) return false;

            if (topHeight > entity.getStepHeight() || topHeight == 0) return false;

            for (int i = 1; i < entity.getHeight() + extraClearance; i++)
                if (!isAir(target.add(up(i))))
                    return false;

            return true;
        }

        private boolean isTopSolid(Vec3d target) {
            if (world.isTopSolid(new BlockPos(target), entity)) return true;
            var solidTopHeight = getSolidTopHeight(target);
            return solidTopHeight >= 0.75d && solidTopHeight <= 1d;
        }

        private double getSolidTopHeight(Vec3d target) {
            var blockPos = new BlockPos(target);
            var blockState = world.getBlockState(blockPos);

            var voxelShape = blockState.getCollisionShape(world, blockPos);

            if (voxelShape.isEmpty()) return 0;

            var maxY = voxelShape.getMax(Direction.Axis.Y);

            var maxYIsFull = VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(0, maxY - 0.001d, 0, 1, maxY, 1), voxelShape, BooleanBiFunction.AND);

            return maxYIsFull ? maxY : -1;
        }

        public Step checkDiagonal(Vec3d from, Vec3d target, Vec3d left, Vec3d right) {
            if (canStandOn(target)) {
                if (isEnoughClearance(left) && isEnoughClearance(right)) return new Step(from, target);
                // can't get there through simple diagonal. another path may be possible around the obstacle or jumping to another block
                return null;
            }

            // can jump from origin block to target.up?
            if (canStandOn(origin, 1) && canStandOn(target.add(PositionHelper.up()))) {
                // need clearance on both sides, but ignore the bottom block
                if (isEnoughClearance(left.add(PositionHelper.up())) && isEnoughClearance(right.add(PositionHelper.up()))) {
                    if (canJumpOver(target))
                        return new Step(from, target.add(PositionHelper.up()));
                }

                return null;
            }

            // can fall from origin block to target.down?
            if (canStandOn(origin) && canStandOn(target.add(PositionHelper.down()), 1)) {
                // need clearance on both sides
                if (isEnoughClearance(left) && isEnoughClearance(right)) return new Step(from, target.add(PositionHelper.down()));

                return null;
            }

            return null;
        }

        public Step checkDiagonalL(Vec3d from, Vec3d target, Vec3d left, Vec3d right, Vec3d leftExtra, Vec3d rightExtra, boolean invertedL) {
            if (canStandOn(target)) {
                // left or right must be standable, all others must be passable
                if ((canStandOn(left) || canStandOn(right)) && isEnoughClearance(left) && isEnoughClearance(right) && isEnoughClearance(leftExtra) && isEnoughClearance(rightExtra))
                    return new Step(from, target);
            }

            // Currently jumping in L is not possible. Maybe with more complex moving.

//            // can jump from origin block to target.up?
//            if (canStandOn(origin, 1) && canStandOn(target.add(PositionHelper.up()))) {
//                // if inverted L, then leftExtra is beside target. otherwise, rightExtra is beside target
//                var extraBesideTarget = invertedL ? leftExtra : rightExtra;
//                var extraBesideOrigin = invertedL ? rightExtra : leftExtra;
//
//                // need clearance on both sides and extra beside origin. also need clearance but one up on extra beside target
//                if (isEnoughClearance(left, 1) && isEnoughClearance(right, 1) && isEnoughClearance(extraBesideOrigin, 1) && isEnoughClearance(extraBesideTarget.add(PositionHelper.up())))
//                    return new Step(from, target.add(PositionHelper.up()), Step.Type.JUMP);
//            }

            // can fall from origin block to target.down?
            if (canStandOn(origin) && canStandOn(target.add(PositionHelper.down()), 1)) {
                // if inverted L, then leftExtra is beside target. otherwise, rightExtra is beside target
                var extraBesideTarget = invertedL ? leftExtra : rightExtra;
                var extraBesideOrigin = invertedL ? rightExtra : leftExtra;

                // need clearance under both sides and extra beside target. also need clearance on extra beside origin
                if (isEnoughClearance(left.add(PositionHelper.down()), 1) && isEnoughClearance(right.add(PositionHelper.down()), 1) && isEnoughClearance(extraBesideTarget.add(PositionHelper.down()), 1) && isEnoughClearance(extraBesideOrigin.add(PositionHelper.up())))
                    return new Step(from, target.add(PositionHelper.down()));
            }

            return null;
        }
    }
}
