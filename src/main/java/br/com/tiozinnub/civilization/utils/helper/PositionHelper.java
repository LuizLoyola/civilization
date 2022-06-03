package br.com.tiozinnub.civilization.utils.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class PositionHelper {
    public static BlockPos firstBlockDown(World world, int x, int y, int z) {
        for (int i = y; i >= 0; i--) {
            BlockPos pos = new BlockPos(x, i, z);
            if (world.getBlockState(pos).isAir()) continue;
            return pos;
        }
        return new BlockPos(x, y, z);
    }

    public static boolean isPosWithin(BlockPos pos, ChunkPos chunk) {
        return pos.getX() >= chunk.getStartX() && pos.getX() <= chunk.getEndX() && pos.getZ() >= chunk.getStartZ() && pos.getZ() <= chunk.getEndZ();
    }


    public static boolean isPosWithin(BlockPos pos, Box box) {
        return isPosWithin(pos, box, false);
    }

    public static boolean isPosWithin(BlockPos pos, Box box, boolean ignoreY) {
        return box.minX <= pos.getX() && box.maxX >= pos.getX() &&
                box.minZ <= pos.getZ() && box.maxZ >= pos.getZ() &&
                (ignoreY || box.minY <= pos.getY() && box.maxY >= pos.getY());
    }
}
