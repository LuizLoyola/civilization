package br.com.tiozinnub.civilization.core.structure;

import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum StructureType implements StringIdentifiable {
    HOUSE;

    @Nullable
    public static StructureType byName(@Nullable String name) {
        return Arrays.stream(values()).filter(e -> e.asString().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }
}
