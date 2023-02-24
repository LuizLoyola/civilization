package br.com.tiozinnub.civilization.entity.base;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.yawBetween;

public class PathfinderService {
    private final NodeViewer nodeViewer;
    public Pathfinder pathfinder; // TODO: CHANGE TO PRIVATE

    public PathfinderService(NodeViewer nodeViewer) {
        this.nodeViewer = nodeViewer;
    }

    public boolean isFindingPath() {
        return this.pathfinder != null && this.pathfinder.isWorking();
    }

    public void startPathfinder(Vec3d start, Vec3d end, double minDistance) {
        if (this.isFindingPath()) {
            if (this.pathfinder != null) {
                // is the same exact path?

                if (this.pathfinder.start.equals(start) && this.pathfinder.end.equals(end) && this.pathfinder.minDistance == minDistance) {
                    return;
                }

                this.pathfinder.cancel();
            }
            this.pathfinder = null;
        }

        this.pathfinder = new Pathfinder(start, end, minDistance);
    }

    public Path findPath(Vec3d start, Vec3d end, double minDistance) {
        this.startPathfinder(start, end, minDistance);
        while (this.isFindingPath()) {
            this.tickUntilFind();
        }
        return this.getPathAndClear();
    }

    public Path getPathAndClear() {
        if (this.pathfinder == null)
            return null;

        var path = this.pathfinder.path;
        if (path != null)
            this.pathfinder = null;
        return path;
    }

    public Path tick() {
        if (!this.isFindingPath()) return null;

        this.pathfinder.tick();

        return this.pathfinder.path;
    }


    public Path tickUntilFind() {
        var startTime = System.currentTimeMillis();
        int maxMsPerWarp = 10;
        Path path = null;

        var count = 0;

        do {
            if (pathfinder.cancelled) break;

            path = this.tick();
            count++;
        } while (path == null && System.currentTimeMillis() - startTime < maxMsPerWarp);

        System.out.println("TICKS: " + count + (pathfinder.cancelled ? " GAVE UP" : path == null ? " " + maxMsPerWarp + "ms PASSED" : " DONE"));

        return path;
    }

    public abstract static class NodeViewer {
        public abstract List<Step> getNeighbors(Vec3d pos);
    }

    public class Pathfinder {
        // TODO: CHANGE TO PRIVATE
        private static final double Y_DIFF_COST = 0.125d;
        private static final double JUMP_COST = Y_DIFF_COST + 1.5d;
        private static final double FALL_COST = Y_DIFF_COST + 0.75d;
        public final ArrayList<Node> nodes; // TODO: CHANGE TO PRIVATE
        private final Vec3d start;
        private final Vec3d end;
        private final double minDistance;
        private final HashSet<Integer> open;
        private final HashSet<Integer> closed;
        private final HashMap<Vec3d, Integer> posMap;
        public Path path;
        private boolean cancelled;

        public Pathfinder(Vec3d start, Vec3d end, double minDistance) {
            this.start = Vec3d.ofBottomCenter(new BlockPos(start));
            this.end = end;
            this.minDistance = minDistance;

            this.nodes = new ArrayList<>();
            this.open = new HashSet<>();
            this.closed = new HashSet<>();
            this.posMap = new HashMap<>();

            if (start.equals(end)) {
                this.path = new Path(start);
                this.path.addStep(new Step(null, start));
                return;
            }

            this.addNode(null, new Step(null, start), 0);
        }

        private void close(int index) {
            this.open.remove(index);
            this.closed.add(index);
        }

        public boolean isOpen(int index) {  // TODO: CHANGE TO PRIVATE
            return this.open.contains(index);
        }

        public boolean isClosed(int index) {  // TODO: CHANGE TO PRIVATE
            return this.closed.contains(index);
        }

        private Node addNode(Node parent, Step step, double cost) {
            return this.addNode(parent, step, cost, -1);
        }

        private Node addNode(Node parent, Step step, double cost, int at) {
            var index = at == -1 ? this.nodes.size() : at;
            var parentCost = parent == null ? 0 : parent.totalCost;
            var parentIndex = parent == null ? -1 : parent.index();
            var node = new Node(index, parentIndex, step.fromPos(), step.toPos(), cost, parentCost + cost, step.toPos().distanceTo(end));

            if (at == -1) {
                this.nodes.add(node);
                this.posMap.put(step.toPos(), index);
            } else {
                this.nodes.set(at, node);
                this.posMap.put(step.toPos(), at);
            }

            this.open.add(index);

            return node;
        }

        private double calculateStepCost(Node previous, Node from, Step to) {
            var multiplier = 1d;
            var yDiff = to.toPos().getY() - from.pos().getY();
            if (yDiff == 1) {
                multiplier *= JUMP_COST;
            } else if (yDiff == -1) {
                multiplier *= FALL_COST;
            }

            if (previous != null) {
                var prevYaw = yawBetween(previous.pos(), from.pos());
                var thisYaw = yawBetween(from.pos(), to.toPos());

                if (prevYaw != thisYaw) {
                    multiplier += (Math.abs(thisYaw - prevYaw) / 3);
                }
            }

            return from.pos().distanceTo(to.toPos()) * multiplier;
        }

        private List<Node> getOpenNodes() {
            var openNodes = new ArrayList<Node>();
            for (var index : this.open) {
                openNodes.add(this.getNode(index));
            }
            return openNodes;
        }

        private List<Node> getClosedNodes() {
            var closedNodes = new ArrayList<Node>();
            for (var index : this.closed) {
                closedNodes.add(this.getNode(index));
            }
            return closedNodes;
        }

        public void cancel() {
            if (this.path == null) this.cancelled = true;
        }

        private void tick() {
            if (!this.isWorking()) return;

            var thisNode = this.getClosestOpenNode();

            if (thisNode == null) {
                this.cancel();
                return;
            }

            Node prevNode = null;
            if (thisNode.parentIndex != -1) {
                prevNode = this.getNode(thisNode.parentIndex);
            }

            var neighbors = nodeViewer.getNeighbors(thisNode.pos);

            close(thisNode.index);

            for (var neighbor : neighbors) {
                // check if this position is already mapped
                var existingNode = this.getNodeAt(neighbor.toPos());
                var isEnd = neighbor.toPos().equals(end);
                var isCloseEnough = this.minDistance > 0 && neighbor.toPos().distanceTo(end) < this.minDistance;

                if (existingNode == null) {
                    // is node even close enough to make it worth it?
                    if (!isEnd && !isCloseEnough && !this.isNodeWorthChecking(neighbor)) continue;

                    // this node doesn't exist, add it
                    var neighborNode = this.addNode(thisNode, neighbor, this.calculateStepCost(prevNode, thisNode, neighbor));

                    if (isEnd || isCloseEnough) {
                        close(neighborNode.index);
                        this.path = this.buildPath(neighborNode.index());
                        return;
                    }
                }

                if (existingNode != null) {
                    // existingNode is at the same position as neighbor
                    // is it better to use existingNode or neighbor?
                    var existingNodeCost = existingNode.totalCost;
                    var neighborCost = this.calculateStepCost(prevNode, thisNode, neighbor) + thisNode.totalCost;

                    if (neighborCost < existingNodeCost) {
                        // neighbor is better, replace existingNode with neighbor
                        this.addNode(thisNode, neighbor, this.calculateStepCost(prevNode, thisNode, neighbor), existingNode.index());
                    }
                }
            }
        }

        private boolean isNodeWorthChecking(Step node) {
            var distanceStartToEnd = start.distanceTo(end);

            var distanceToEnd = node.toPos().distanceTo(end);
            var distanceToStart = node.toPos().distanceTo(start);

            // if the distance is too small, just check it
            var passDistance = 10;

            if (distanceToEnd < passDistance || distanceToStart < passDistance) return true;

            // tolerance is 1.5x the distance from start to end
            var tolerance = 1.5;
            distanceStartToEnd *= tolerance;

            // if the distance node to start or node to end is greater than the distance from start to end (adding tolerance), it's not worth checking
            return distanceToEnd < distanceStartToEnd || distanceToStart < distanceStartToEnd;
        }

        private Node getNodeAt(Vec3d pos) {
//            return this.nodes.stream().filter(node -> node.toPos.equals(toPos)).findFirst().orElse(null);
            var index = this.posMap.get(pos);
            return index == null ? null : this.getNode(index);
        }

        private Path buildPath(int endNodeIndex) {
            List<Node> pathNodesInverted = new ArrayList<>();
            var currentNode = this.getNode(endNodeIndex);

            while (currentNode.parentIndex != -1) {
                pathNodesInverted.add(currentNode);
                currentNode = this.getNode(currentNode.parentIndex);
            }

            var path = new Path(this.start);

            for (int i = pathNodesInverted.size() - 1; i >= 0; i--) {
                path.addStep(pathNodesInverted.get(i).asStep());
            }

            return path;
        }

        private Node getNode(int index) {
            return this.nodes.get(index);
        }

        private Node getClosestOpenNode() {
            if (this.open.isEmpty()) return null;

            var cheaper = this.getNode(this.open.iterator().next());

            for (var index : this.open) {
                var node = this.getNode(index);

                if (node.heuristicCost() < cheaper.heuristicCost()) {
                    cheaper = node;
                }
            }
            return cheaper;
        }

        public boolean isWorking() {
            return !this.cancelled && this.path == null;
        }

        // TODO: CHANGE TO PRIVATE
        public record Node(int index, int parentIndex, Vec3d from, Vec3d pos, double stepCost, double totalCost, double distToEnd) {
            public Step asStep() {
                return new Step(from, pos);
            }

            public double heuristicCost() {
                return this.totalCost / 5 + this.distToEnd;
            }
        }
    }

}


