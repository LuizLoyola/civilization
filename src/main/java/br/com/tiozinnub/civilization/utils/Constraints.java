package br.com.tiozinnub.civilization.utils;

import br.com.tiozinnub.civilization.CivilizationMod;
import net.minecraft.util.Identifier;

public class Constraints {
    public static Identifier idFor(String name) {
        return new Identifier(CivilizationMod.MOD_ID, name);
    }
}
