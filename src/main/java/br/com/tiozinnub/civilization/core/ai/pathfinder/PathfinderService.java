package br.com.tiozinnub.civilization.core.ai.pathfinder;

import com.google.common.collect.Sets;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        Path path;

        do {
            path = this.tick();
        } while (path == null && System.currentTimeMillis() - startTime < maxMsPerWarp);

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
        public Path path;
        private boolean cancelled;
        private Node currNode;
        private List<Step> neighbors;
        private int neighborsIndex;

        public Pathfinder(BlockPos start, BlockPos end) {
            this.start = start;
            this.end = end;

            this.nodes = Lists.newArrayList();
            this.open = Sets.newHashSet();
            this.closed = Sets.newHashSet();

            this.open(this.addNode(-1, start, start, Step.Type.START, -1));
        }

        private int open(int index) {
            this.open.add(index);
            return index;
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

        private int addNode(int parentIndex, Step step, int overrideIndex) {
            return this.addNode(parentIndex, step.pos(), this.getNode(parentIndex).pos, step.type(), overrideIndex);
        }

        private int addNode(int parentIndex, BlockPos pos, BlockPos parentBlockPos, Step.Type type, int overrideIndex) {
            var index = overrideIndex == -1 ? this.nodes.size() : overrideIndex;
            var node = new Node(index, parentIndex, pos, type, this.calculateCostTo(pos, parentBlockPos, type), this.calculateCostFrom(pos));
            if (overrideIndex == -1) {
                this.nodes.add(node);
            } else {
                this.nodes.set(overrideIndex, node);
            }
            return node.index;
        }

        private double calculateCostFrom(BlockPos pos) {
            return pos.getSquaredDistance(this.end);
        }

        private double calculateCostTo(BlockPos pos, BlockPos parentBlockPos, Step.Type type) {
            return parentBlockPos.getSquaredDistance(pos) * type.costMultiplier;
        }

        public void cancel() {
            if (this.path == null) this.cancelled = true;
        }

        private void tick() {
            if (!this.isWorking()) return;

            if (this.neighbors == null || this.neighborsIndex >= this.neighbors.size()) {
                this.currNode = this.getCheaperOpenNode();

                // no nodes?
                if (this.currNode == null) {
                    this.cancel();
                    return;
                }

                close(this.currNode.index);

                this.neighbors = nodeViewer.getNeighbors(this.currNode.pos);
                this.neighborsIndex = 0;
            }

            var neighbor = this.neighbors.get(this.neighborsIndex++);

            // if this neighbor is 1.25x further than the startup position is from the end position, it's too far
            if (neighbor.pos().getSquaredDistance(this.start) > this.end.getSquaredDistance(this.start) * 1.25) {
                return;
            }

            // if there is a node with the same position as this neighbor: if it's closed, skip it, if it's open, check if it's cheaper and replace it if it is
            var existingNode = this.nodes.stream().filter(n -> n.pos.equals(neighbor.pos())).findFirst().orElse(null);
            var overrideIndex = -1;
            if (existingNode != null) {
                if (this.isClosed(existingNode.index)) return;

                if (this.isOpen(existingNode.index)) {
                    if (existingNode.costTo < this.currNode.costTo) return;

                    overrideIndex = existingNode.index;
                }
            }

            var neighborIndex = open(addNode(this.currNode.index, neighbor, overrideIndex));

            // can equals be used here?
            if (neighbor.pos().equals(this.end)) {
                close(neighborIndex);
                this.path = this.buildPath(neighborIndex);
            }
        }

        private Path buildPath(int endNodeIndex) {
            List<Node> pathNodesInverted = Lists.newArrayList();
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

        private Node getCheaperOpenNode() {
            if (this.open.isEmpty()) return null;

            var cheaper = this.getNode(this.open.iterator().next());

            for (var index : this.open) {
                var node = this.getNode(index);
                if (node.cost() < cheaper.cost()) {
                    cheaper = node;
                }
            }
            return cheaper;
        }

        public boolean isWorking() {
            return !this.cancelled && this.path == null;
        }

        // TODO: CHANGE TO PRIVATE
        public record Node(int index, int parentIndex, BlockPos pos, Step.Type type, double costTo, double costFrom) {
            public Step asStep() {
                return new Step(this.pos, this.type);
            }

            public double cost() {
                return this.costTo + this.costFrom;
            }
        }
    }

}


