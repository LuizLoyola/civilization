package br.com.tiozinnub.civilization.core.ai.pathfinder;

import net.minecraft.util.math.BlockPos;

public record Step(BlockPos pos, Type type) {
    public enum Type {

        START('S', 0),
        WALK('W', 1),
        JUMP('J', 1.5),
        FALL('F', 0.75);

        public final double costMultiplier;
        public final char c;

        Type(char c, double costMultiplier) {
            this.c = c;
            this.costMultiplier = costMultiplier;
        }

        public static Type fromChar(char c) {
            for (Type type : Type.values()) {
                if (type.c == c) {
                    return type;
                }
            }
            return null;
        }
    }
}
