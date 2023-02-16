package br.com.tiozinnub.civilization.core.ai.pathfinder;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class WorldNodeViewer extends PathfinderService.NodeViewer {
    private final ServerWorld world;

    public WorldNodeViewer(ServerWorld world) {
        this.world = world;
    }

    @Override
    public List<Step> getNeighbors(BlockPos pos) {
        return List.of(
                new Step(pos.up(), Step.Type.WALK),
                new Step(pos.down(), Step.Type.WALK),
                new Step(pos.north(), Step.Type.WALK),
                new Step(pos.south(), Step.Type.WALK),
                new Step(pos.east(), Step.Type.WALK),
                new Step(pos.west(), Step.Type.WALK)
        );
    }
}
