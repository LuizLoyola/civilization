package br.com.tiozinnub.civilization.core.structure;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.util.math.Box;

public abstract class Structure extends Serializable {
    private Box box;

    public abstract StructureType getType();

    public abstract int getLevel();

    public abstract int getMaxLevel();

    public Box getBox() {
        return this.box;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("box", this::getBox, b -> this.box = b, new Box(0, 0, 0, 0, 0, 0));
    }
}
