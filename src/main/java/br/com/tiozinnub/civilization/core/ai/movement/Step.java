package br.com.tiozinnub.civilization.core.ai.movement;

import net.minecraft.util.math.BlockPos;

public record Step(BlockPos pos, Type type) {
    public enum Type {

        START('S', true, 0),
        WALK('W', true, 1),
        JUMP('J', true, 1.5),
        FALL('F', true, 1);

        public final double costMultiplier;
        public final char c;
        private final boolean requiresJump;

        Type(char c, boolean requiresJump, double costMultiplier) {
            this.c = c;
            this.requiresJump = requiresJump;
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

        public boolean requiresJump() {
            return this.requiresJump;
        }
    }
}
