package br.com.tiozinnub.civilization.core.math;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class Pos2d {
    public final int x;
    public final int z;

    public Pos2d(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public static Pos2d from(BlockPos blockPos) {
        return new Pos2d(blockPos.getX(), blockPos.getZ());
    }

    public EnumSet<CardinalDirection> getDirectionsTo(Pos2d other) {
        if (this.x == other.x && this.z == other.z) {
            return EnumSet.noneOf(CardinalDirection.class);
        }

        if (this.x == other.x) {
            if (this.z > other.z) {
                return EnumSet.of(CardinalDirection.NORTH);
            } else {
                return EnumSet.of(CardinalDirection.SOUTH);
            }
        } else if (this.z == other.z) {
            if (this.x > other.x) {
                return EnumSet.of(CardinalDirection.WEST);
            } else {
                return EnumSet.of(CardinalDirection.EAST);
            }
        } else {
            if (this.x > other.x) {
                if (this.z > other.z) {
                    return CardinalDirection.NORTH_WEST;
                } else {
                    return CardinalDirection.SOUTH_WEST;
                }
            } else {
                if (this.z > other.z) {
                    return CardinalDirection.NORTH_EAST;
                } else {
                    return CardinalDirection.SOUTH_EAST;
                }
            }
        }
    }

    public BlockPos asBlockPos(int y) {
        return new BlockPos(this.x, y, this.z);
    }

    public BlockPos asBlockPos() {
        return asBlockPos(0);
    }
}
