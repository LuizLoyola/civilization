package br.com.tiozinnub.civilization.entity.person.property;

import br.com.tiozinnub.civilization.utils.helper.RandomHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.random.Random;

import java.util.Locale;

public enum Gender implements StringIdentifiable {
    MALE,
    FEMALE;

    public static Gender fromString(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public static Gender randomGender(Random random) {
        return RandomHelper.pickOne(random, MALE, FEMALE);
    }

    public static <T> T byGender(Gender gender, T ifMale, T ifFemale) {
        return gender == MALE ? ifMale : ifFemale;
    }

    public static <T> T byGender(IGendered gendered, T ifMale, T ifFemale) {
        return byGender(gendered.getGender(), ifMale, ifFemale);
    }

    public Gender opposite() {
        return this == MALE ? FEMALE : MALE;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

}

