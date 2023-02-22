package br.com.tiozinnub.civilization.core.ai.movement;

import net.minecraft.util.math.BlockPos;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.yawBetween;

public record Step(BlockPos fromPos, BlockPos toPos) {
    public double yaw() {
        if (fromPos == null) return 0d;
        return yawBetween(fromPos, toPos);
    }

    public int yDiff() {
        if (fromPos == null) return 0;
        return toPos.getY() - fromPos.getY();
    }
}
