package br.com.tiozinnub.civilization.core.ai.movement.pathing;

import br.com.tiozinnub.civilization.core.ai.movement.Step;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.getDistance;

public class PathfinderService {
    private final NodeViewer nodeViewer;
    public Pathfinder pathfinder; // TODO: CHANGE TO PRIVATE

    public PathfinderService(NodeViewer nodeViewer) {
        this.nodeViewer = nodeViewer;
    }

    public boolean isFindingPath() {
        return this.pathfinder != null && this.pathfinder.isWorking();
    }

    public void startPathfinder(BlockPos start, BlockPos end) {
        if (this.isFindingPath()) {
            if (this.pathfinder != null) {
                this.pathfinder.cancel();
            }
            this.pathfinder = null;
        }

        this.pathfinder = new Pathfinder(start, end);
    }

    public Path findPath(BlockPos start, BlockPos end) {
        this.startPathfinder(start, end);
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
        public abstract List<Step> getNeighbors(BlockPos pos);
    }

    public class Pathfinder { // TODO: CHANGE TO PRIVATE
        public final ArrayList<Node> nodes; // TODO: CHANGE TO PRIVATE
        private final BlockPos start;
        private final BlockPos end;
        private final HashSet<Integer> open;
        private final HashSet<Integer> closed;
        private final HashMap<BlockPos, Integer> posMap;
        public Path path;
        private boolean cancelled;

        public Pathfinder(BlockPos start, BlockPos end) {
            this.start = start;
            this.end = end;

            this.nodes = new ArrayList<>();
            this.open = new HashSet<>();
            this.closed = new HashSet<>();
            this.posMap = new HashMap<>();

            if (start.equals(end)) {
                this.path = new Path(start);
                this.path.addStep(new Step(start, Step.Type.START));
                this.path.addStep(new Step(start, Step.Type.WALK));
                return;
            }

            this.addNode(null, new Step(start, Step.Type.START), 0);
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
            var node = new Node(index, parentIndex, step.pos(), step.type(), cost, parentCost + cost, getDistance(step.pos(), end));

            if (at == -1) {
                this.nodes.add(node);
                this.posMap.put(step.pos(), index);
            } else {
                this.nodes.set(at, node);
                this.posMap.put(step.pos(), at);
            }

            this.open.add(index);

            return node;
        }

        private double calculateStepCost(Node from, Step to) {
            return getDistance(from.pos(), to.pos()) * to.type().costMultiplier;
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

            var neighbors = nodeViewer.getNeighbors(thisNode.pos);

            close(thisNode.index);

            for (var neighbor : neighbors) {
                // check if this position is already mapped
                var existingNode = this.getNodeAt(neighbor.pos());
                var isEnd = neighbor.pos().equals(end);

                if (existingNode == null) {
                    // is node even close enough to make it worth it?
                    if (!isEnd && !this.isNodeWorthChecking(neighbor)) continue;

                    // this node doesn't exist, add it
                    var neighborNode = this.addNode(thisNode, neighbor, this.calculateStepCost(thisNode, neighbor));

                    if (isEnd) {
                        close(neighborNode.index);
                        this.path = this.buildPath(neighborNode.index());
                        return;
                    }
                }

                if (existingNode != null) {
                    // existingNode is at the same position as neighbor
                    // is it better to use existingNode or neighbor?
                    var existingNodeCost = existingNode.totalCost;
                    var neighborCost = this.calculateStepCost(thisNode, neighbor) + thisNode.totalCost;

                    if (neighborCost < existingNodeCost) {
                        // neighbor is better, replace existingNode with neighbor
                        this.addNode(thisNode, neighbor, this.calculateStepCost(thisNode, neighbor), existingNode.index());
                    }
                }
            }
        }

        private boolean isNodeWorthChecking(Step node) {
            var distanceStartToEnd = getDistance(start, end);

            var distanceToEnd = getDistance(node.pos(), end);
            var distanceToStart = getDistance(node.pos(), start);

            // if the distance is too small, just check it
            var passDistance = 10;

            if (distanceToEnd < passDistance || distanceToStart < passDistance) return true;

            // tolerance is 1.5x the distance from start to end
            var tolerance = 1.5;
            distanceStartToEnd *= tolerance;

            // if the distance node to start or node to end is greater than the distance from start to end (adding tolerance), it's not worth checking
            return distanceToEnd < distanceStartToEnd || distanceToStart < distanceStartToEnd;
        }

        private Node getNodeAt(BlockPos pos) {
//            return this.nodes.stream().filter(node -> node.pos.equals(pos)).findFirst().orElse(null);
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
                if (node.distToEnd < cheaper.distToEnd) {
                    cheaper = node;
                }
            }
            return cheaper;
        }

        public boolean isWorking() {
            return !this.cancelled && this.path == null;
        }

        // TODO: CHANGE TO PRIVATE
        public record Node(int index, int parentIndex, BlockPos pos, Step.Type type, double stepCost, double totalCost, double distToEnd) {
            public Step asStep() {
                return new Step(this.pos, this.type);
            }

        }
    }

}


