package br.com.tiozinnub.civilization.core.ai.pathfinder;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class WorldNodeViewer extends PathfinderService.NodeViewer {
    private final ServerWorld world;

    public WorldNodeViewer(ServerWorld world) {
        this.world = world;
    }

    @Override
    public List<Step> getNeighbors(BlockPos pos) {
        var maxDistance = 3;
        // get all possible positions within maxDistance (ignoring y)

        var neighbors = new ArrayList<Step>();
        for (int x = -maxDistance; x <= maxDistance; x++) {
            for (int z = -maxDistance; z <= maxDistance; z++) {
                var neighborPos = pos.add(x, 0, z);
                if (neighborPos.equals(pos)) continue;
                neighbors.add(new Step(neighborPos, Step.Type.WALK));
            }
        }

        return neighbors;
    }
}
