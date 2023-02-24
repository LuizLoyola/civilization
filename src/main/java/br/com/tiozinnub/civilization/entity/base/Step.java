package br.com.tiozinnub.civilization.entity.base;

import net.minecraft.util.math.Vec3d;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.yawBetween;

public record Step(Vec3d fromPos, Vec3d toPos) {
    public double yaw() {
        if (fromPos == null) return 0d;
        return yawBetween(fromPos, toPos);
    }

    public double yDiff() {
        if (fromPos == null) return 0;
        return toPos.getY() - fromPos.getY();
    }
}
