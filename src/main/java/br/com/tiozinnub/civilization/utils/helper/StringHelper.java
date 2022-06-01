package br.com.tiozinnub.civilization.utils.helper;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public class StringHelper {
    public static String getStringFromVec3d(Vec3d vector) {
        return getStringFromVec3d(vector, false);
    }

    public static String getStringFromVec3d(Vec3d vector, boolean asInt) {
        return getStringFromVec3d(vector, asInt, false);
    }

    public static String getStringFromVec3d(Vec3d vector, boolean asInt, boolean wrap) {
        return getStringFromVec3d(vector, asInt, wrap, false);
    }

    public static String getStringFromVec3d(Vec3d vector, boolean asInt, boolean wrap, boolean verbose) {
        if (vector != null)
            return buildString(vector.getX(), vector.getY(), vector.getZ(), asInt, wrap, verbose, false);
        return buildString(null, null, null, asInt, wrap, verbose, false);
    }

    public static String getStringFromBlockPos(BlockPos pos) {
        return getStringFromBlockPos(pos, false);
    }

    public static String getStringFromBlockPos(BlockPos pos, boolean wrap) {
        return getStringFromBlockPos(pos, wrap, false);
    }

    public static String getStringFromBlockPos(BlockPos pos, boolean wrap, boolean verbose) {
        if (pos != null)
            return buildString((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), true, wrap, verbose, false);
        return buildString(null, null, null, true, wrap, verbose, false);
    }

    public static String getStringFromChunkPos(ChunkPos pos) {
        return getStringFromChunkPos(pos, false);
    }

    public static String getStringFromChunkPos(ChunkPos pos, boolean wrap) {
        return getStringFromChunkPos(pos, wrap, false);
    }

    public static String getStringFromChunkPos(ChunkPos pos, boolean wrap, boolean verbose) {
        if (pos != null)
            return buildString((double) pos.x, 0d, (double) pos.z, true, wrap, verbose, true);
        return buildString(null, null, null, true, wrap, verbose, false);
    }


    private static String buildString(Double x, Double y, Double z, boolean asInt, boolean wrap, boolean verbose, boolean hideY) {
        StringBuilder b = new StringBuilder();
        if (wrap) b.append("[");

        b.append(verbose ? "X: " : "");
        if (x == null) b.append("null");
        else b.append(asInt ? String.valueOf(x.intValue()) : String.valueOf(x));
        if (!hideY) {
            b.append(verbose ? ", Y: " : " ");
            if (y == null) b.append("null");
            else b.append(asInt ? String.valueOf(y.intValue()) : String.valueOf(y));
        }
        b.append(verbose ? ", Z: " : " ");
        if (z == null) b.append("null");
        else b.append(asInt ? String.valueOf(z.intValue()) : String.valueOf(z));

        if (wrap) b.append("]");

        return b.toString();
    }


}
