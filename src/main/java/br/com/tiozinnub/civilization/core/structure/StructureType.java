package br.com.tiozinnub.civilization.core.structure;

import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;

public enum StructureType implements StringIdentifiable {
    HOUSE;

    public static StructureType fromString(String string) {
        return Arrays.stream(values()).filter(type -> type.asString().equals(string)).findFirst().orElse(null);
    }

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }
}
