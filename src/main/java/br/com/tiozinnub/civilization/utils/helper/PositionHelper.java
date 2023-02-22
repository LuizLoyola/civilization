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

    public static Vec3d north() {
        return north(1);
    }

    public static Vec3d north(double distance) {
        return new Vec3d(0, 0, distance);
    }

    public static Vec3d south() {
        return south(1);
    }

    public static Vec3d south(double distance) {
        return new Vec3d(0, 0, -distance);
    }

    public static Vec3d east() {
        return east(1);
    }

    public static Vec3d east(double distance) {
        return new Vec3d(distance, 0, 0);
    }

    public static Vec3d west() {
        return west(1);
    }

    public static Vec3d west(double distance) {
        return new Vec3d(-distance, 0, 0);
    }

    public static Vec3d up() {
        return up(1);
    }

    public static Vec3d up(double distance) {
        return new Vec3d(0, distance, 0);
    }

    public static Vec3d down() {
        return down(1);
    }

    public static Vec3d down(double distance) {
        return new Vec3d(0, -distance, 0);
    }
}
