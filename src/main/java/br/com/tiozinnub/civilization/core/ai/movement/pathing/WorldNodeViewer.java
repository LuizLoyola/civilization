package br.com.tiozinnub.civilization.core.ai.movement.pathing;

import br.com.tiozinnub.civilization.core.ai.movement.Step;
import br.com.tiozinnub.civilization.entity.EntityBase;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class WorldNodeViewer extends PathfinderService.NodeViewer {
    private final ServerWorld world;
    private final EntityBase entity;
    private final int entityHeight;

    public WorldNodeViewer(ServerWorld world, EntityBase entity) {
        this.world = world;
        this.entity = entity;
        this.entityHeight = (int) Math.ceil(entity.getHeight());
    }

    @Override
    public List<Step> getNeighbors(BlockPos pos) {
        // if is on air, go down until find a block

        var p = pos;
        while (world.isAir(p)) {
            p = p.down();

            // somehow, the block is on the void
            if (p.getY() < world.getBottomY()) {
                p = pos;
                break;
            }
        }

        var n = new NodeFinder(p.up());

        // CARDINALS
        BlockPos north = pos;
        BlockPos east = pos;
        BlockPos south = pos;
        BlockPos west = pos;

        int maxWalkDist = 3;

        for (int i = 1; i <= maxWalkDist; i++) {
            north = north == null ? null : n.add(n.checkCardinal(north.north()));
            east = east == null ? null : n.add(n.checkCardinal(east.east()));
            south = south == null ? null : n.add(n.checkCardinal(south.south()));
            west = west == null ? null : n.add(n.checkCardinal(west.west()));
        }

        // PERFECT DIAGONALS
        BlockPos northEast = pos;
        BlockPos southEast = pos;
        BlockPos southWest = pos;
        BlockPos northWest = pos;

        for (int i = 1; i <= maxWalkDist; i++) {
            var ne = pos.east(i).north(i);
            northEast = northEast == null ? null : n.add(n.checkWalkDiagonal(ne, ne.east(-1), ne.north(-1)));
            var se = pos.south(i).east(i);
            southEast = southEast == null ? null : n.add(n.checkWalkDiagonal(se, se.south(-1), se.east(-1)));
            var sw = pos.west(i).south(i);
            southWest = southWest == null ? null : n.add(n.checkWalkDiagonal(sw, sw.west(-1), sw.south(-1)));
            var nw = pos.north(i).west(i);
            northWest = northWest == null ? null : n.add(n.checkWalkDiagonal(nw, nw.north(-1), nw.west(-1)));
        }

        // L DIAGONALS


        var nne = pos.east(1).north(2);
        n.add(n.checkWalkDiagonalL(nne, pos.north(1), pos.north(1).east(1), pos.north(2), pos.east(1), true));

        var nee = pos.east(2).north(1);
        n.add(n.checkWalkDiagonalL(nee, pos.east(1).north(1), pos.east(1), pos.north(1), pos.east(2), false));

        var see = pos.east(2).south(1);
        n.add(n.checkWalkDiagonalL(see, pos.east(1), pos.east(1).south(1), pos.east(2), pos.south(1), true));

        var sse = pos.east(1).south(2);
        n.add(n.checkWalkDiagonalL(sse, pos.south(1).east(1), pos.south(1), pos.east(1), pos.south(2), false));

        var ssw = pos.west(1).south(2);
        n.add(n.checkWalkDiagonalL(ssw, pos.south(1), pos.south(1).west(1), pos.south(2), pos.west(1), true));

        var sww = pos.west(2).south(1);
        n.add(n.checkWalkDiagonalL(sww, pos.south(1).west(1), pos.west(1), pos.south(1), pos.west(2), false));

        var nww = pos.west(2).north(1);
        n.add(n.checkWalkDiagonalL(nww, pos.west(1), pos.west(1).north(1), pos.west(2), pos.north(1), true));

        var nnw = pos.west(1).north(2);
        n.add(n.checkWalkDiagonalL(nnw, pos.north(1).west(1), pos.north(1), pos.west(1), pos.north(2), false));

        return n.nodeList;
    }

    private class NodeFinder {
        private final ArrayList<Step> nodeList = new ArrayList<>();
        private final BlockPos origin;

        public NodeFinder(BlockPos origin) {
            this.origin = origin;
        }

        public BlockPos add(Step step) {
            if (step == null) return null;
            nodeList.add(step);
            // if it is not WALK, then return null to avoid trying to walk right after jump/fall
            return step.type() == Step.Type.WALK ? step.pos() : null;
        }

        public Step checkCardinal(BlockPos pos) {
            if (canStandOn(pos)) return new Step(pos, Step.Type.WALK);

            // can jump from origin block to pos.up?
            if (canStandOn(origin, 1) && canStandOn(pos.up())) return new Step(pos.up(), Step.Type.JUMP);

            // can fall from origin block to pos.down?
            if (canStandOn(origin) && canStandOn(pos.down(), 1)) return new Step(pos.down(), Step.Type.FALL);

            return null;
        }

        private boolean canStandOn(BlockPos target) {
            return canStandOn(target, 0);
        }

        private boolean canStandOn(BlockPos target, int extraClearance) {
            if (!isTopSolid(target.down())) return false;
            return isEnoughClearance(target, extraClearance);
        }

        private boolean isEnoughClearance(BlockPos target) {
            return isEnoughClearance(target, 0);
        }

        private boolean isEnoughClearance(BlockPos target, int extraClearance) {
            for (int i = 0; i < entityHeight + extraClearance; i++) if (!world.isAir(target.up(i))) return false;
            return true;
        }

        private boolean isTopSolid(BlockPos target) {
            return world.isTopSolid(target, entity);
        }

        public Step checkWalkDiagonal(BlockPos target, BlockPos left, BlockPos right) {
            if (canStandOn(target)) {
                if (isEnoughClearance(left) && isEnoughClearance(right)) return new Step(target, Step.Type.WALK);
                // can't get there through simple diagonal. another path may be possible around the obstacle or jumping to another block
                return null;
            }

            // can jump from origin block to target.up?
            if (canStandOn(origin, 1) && canStandOn(target.up())) {
                // need clearance on both sides, but ignore the bottom block
                if (isEnoughClearance(left.up()) && isEnoughClearance(right.up())) return new Step(target.up(), Step.Type.JUMP);

                return null;
            }

            // can fall from origin block to target.down?
            if (canStandOn(origin) && canStandOn(target.down(), 1)) {
                // need clearance on both sides
                if (isEnoughClearance(left) && isEnoughClearance(right)) return new Step(target.down(), Step.Type.FALL);

                return null;
            }

            return null;
        }

        public Step checkWalkDiagonalL(BlockPos target, BlockPos left, BlockPos right, BlockPos leftExtra, BlockPos rightExtra, boolean invertedL) {
            if (canStandOn(target)) {
                // left or right must be standable, all others must be passable
                if ((canStandOn(left) || canStandOn(right)) && isEnoughClearance(left) && isEnoughClearance(right) && isEnoughClearance(leftExtra) && isEnoughClearance(rightExtra))
                    return new Step(target, Step.Type.WALK);
            }

            // Currently jumping in L is not possible. Maybe with more complex moving.

//            // can jump from origin block to target.up?
//            if (canStandOn(origin, 1) && canStandOn(target.up())) {
//                // if inverted L, then leftExtra is beside target. otherwise, rightExtra is beside target
//                var extraBesideTarget = invertedL ? leftExtra : rightExtra;
//                var extraBesideOrigin = invertedL ? rightExtra : leftExtra;
//
//                // need clearance on both sides and extra beside origin. also need clearance but one up on extra beside target
//                if (isEnoughClearance(left, 1) && isEnoughClearance(right, 1) && isEnoughClearance(extraBesideOrigin, 1) && isEnoughClearance(extraBesideTarget.up()))
//                    return new Step(target.up(), Step.Type.JUMP);
//            }

            // can fall from origin block to target.down?
            if (canStandOn(origin) && canStandOn(target.down(), 1)) {
                // if inverted L, then leftExtra is beside target. otherwise, rightExtra is beside target
                var extraBesideTarget = invertedL ? leftExtra : rightExtra;
                var extraBesideOrigin = invertedL ? rightExtra : leftExtra;

                // need clearance under both sides and extra beside target. also need clearance on extra beside origin
                if (isEnoughClearance(left.down(), 1) && isEnoughClearance(right.down(), 1) && isEnoughClearance(extraBesideTarget.down(), 1) && isEnoughClearance(extraBesideOrigin.up()))
                    return new Step(target.down(), Step.Type.FALL);
            }

            return null;
        }
    }
}
