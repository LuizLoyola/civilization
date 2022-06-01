package br.com.tiozinnub.civilization.utils;

import net.minecraft.util.Identifier;

public class Constraints {
    public static final String MOD_ID = "civilization";
    public static final String MOD_NAME = "Civilization";
    public static final String MOD_VERSION = "0.0.1";

    public static Identifier idFor(String name) {
        return new Identifier(MOD_ID, name);
    }
}
