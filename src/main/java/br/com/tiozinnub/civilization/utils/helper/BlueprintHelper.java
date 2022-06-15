package br.com.tiozinnub.civilization.utils.helper;

import br.com.tiozinnub.civilization.core.blueprinting.Blueprint;
import br.com.tiozinnub.civilization.utils.CardinalDirection;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.getAllPositions;

public class BlueprintHelper {
    public static void instaBuildBlueprint(ServerWorld world, Blueprint blueprint, Box box, CardinalDirection direction) {
        var b = blueprint.rotate(direction, false);
        if (b.getLengthX() != box.getXLength() + 1 || b.getLengthY() != box.getYLength() + 1 || b.getLengthZ() != box.getZLength() + 1) {
            throw new IllegalStateException("Blueprint size does not match place size");
        }

        var offset = new BlockPos(box.minX, box.minY, box.minZ);

        for (BlockPos pos : getAllPositions(box)) {
            try {
                var blockState = b.getBlockState(pos.subtract(offset));
                if (!blockState.isAir()) {
                    world.setBlockState(pos, blockState);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
