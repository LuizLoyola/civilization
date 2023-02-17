package br.com.tiozinnub.civilization.utils.helper;

import net.minecraft.util.math.BlockPos;

public class PositionHelper {
    public static double getDistance(BlockPos pos1, BlockPos pos2) {
        return Math.sqrt(
                Math.pow(pos2.getX() - pos1.getX(), 2) +
                Math.pow(pos2.getY() - pos1.getY(), 2) +
                Math.pow(pos2.getZ() - pos1.getZ(), 2)
        );
    }
}
