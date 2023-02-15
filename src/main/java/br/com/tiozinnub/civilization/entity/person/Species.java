package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.utils.helper.RandomHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.random.Random;

import java.util.EnumSet;
import java.util.Locale;

public enum Species implements StringIdentifiable {
    HUMAN;

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static Species byName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public static Species randomSpecies(Random random) {
        return RandomHelper.pickOne(random, EnumSet.allOf(Species.class));
    }

}

