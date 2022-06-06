package br.com.tiozinnub.civilization.core.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public abstract class Structure {
    public BlockPos posStart;
    public BlockPos posEnd;

    public Box getBox() {
        return new Box(posStart, posEnd);
    }
}
