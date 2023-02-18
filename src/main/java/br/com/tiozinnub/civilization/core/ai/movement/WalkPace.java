package br.com.tiozinnub.civilization.core.ai.movement;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum WalkPace implements StringIdentifiable {
    SNEAKY(0.4625f),
    SLOW(0.6f),
    DEFAULT(1f),
    RUN(1.5f),
    RUN_JUMP(1.5f);

    private final float speed;

    WalkPace(float speed) {
        this.speed = speed;
    }

    public static WalkPace fromString(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public float getSpeed() {
        return this.speed;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
