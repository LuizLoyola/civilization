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
    private double tickWarpWait = 0;

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
            this.pathfinder.tick();
        }

        return this.pathfinder.path;
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
        return this.tickWarp(1);
    }

    public Path tickWarp(double multiplier) {
        if (!this.isFindingPath()) {
            return this.pathfinder != null ? this.pathfinder.path : null;

        }

        if (multiplier <= 0) return null;

        tickWarpWait += multiplier;

        while (tickWarpWait >= 1) {
            tickWarpWait--;
            this.pathfinder.tick();
            if (!this.isFindingPath())
                return this.pathfinder.path;
        }

        return this.pathfinder.path;
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

        public Pathfinder(BlockPos start, BlockPos end) {
            this.start = start;
            this.end = end;

            this.nodes = Lists.newArrayList();
            this.open = Sets.newHashSet();
            this.closed = Sets.newHashSet();

            this.open(this.addNode(-1, start, start, Step.Type.START));
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

        private int addNode(int parentIndex, Step step) {
            return this.addNode(parentIndex, step.pos(), step.type());
        }

        private int addNode(int parentIndex, BlockPos pos, Step.Type type) {
            return this.addNode(parentIndex, pos, this.getNode(parentIndex).pos, type);
        }

        private int addNode(int parentIndex, BlockPos pos, BlockPos parentBlockPos, Step.Type type) {
            var node = new Node(this.nodes.size(), parentIndex, pos, type, this.calculateCostTo(pos, parentBlockPos, type), this.calculateCostFrom(pos));
            this.nodes.add(node);
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

            var node = this.getCheaperOpenNode();

            close(node.index);

            for (var neighbor : nodeViewer.getNeighbors(node.pos)) {
                var neighborIndex = open(addNode(node.index, neighbor));

                // can equals be used here?
                if (neighbor.pos().equals(this.end)) {
                    close(neighborIndex);
                    this.path = this.buildPath(neighborIndex);
                    return;
                }
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


