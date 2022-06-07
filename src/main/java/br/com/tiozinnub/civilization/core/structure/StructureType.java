package br.com.tiozinnub.civilization.core.structure;

import net.minecraft.util.StringIdentifiable;

public enum StructureType implements StringIdentifiable {
    HOUSE;

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }
}
