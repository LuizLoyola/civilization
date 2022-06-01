package br.com.tiozinnub.civilization.entity.property;

import br.com.tiozinnub.civilization.utils.helper.RandomHelper;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;
import java.util.Random;

public enum Gender implements StringIdentifiable {
    MALE,
    FEMALE;

    public Gender opposite() {
        return this == MALE ? FEMALE : MALE;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static Gender randomGender(Random random) {
        return RandomHelper.pickOne(random, MALE, FEMALE);
    }

    public static <T> T byGender(Gender gender, T ifMale, T ifFemale) {
        return gender == MALE ? ifMale : ifFemale;
    }

}

