package br.com.tiozinnub.civilization.utils.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.atan2;

public class PositionHelper {
    public static double getDistance(BlockPos pos1, BlockPos pos2) {
        return Math.sqrt(
                Math.pow(pos2.getX() - pos1.getX(), 2) +
                Math.pow(pos2.getY() - pos1.getY(), 2) +
                Math.pow(pos2.getZ() - pos1.getZ(), 2)
        );
    }

    public static double yawBetween(Vec3d from, Vec3d to) {
        var dX = from.getX() - to.getX();
        var dZ = from.getZ() - to.getZ();

        return atan2(dX, dZ);
    }

    public static double yawBetween(BlockPos from, BlockPos to) {
        return yawBetween(from.toCenterPos(), to.toCenterPos());
    }
}
