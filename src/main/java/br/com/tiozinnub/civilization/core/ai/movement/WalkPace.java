package br.com.tiozinnub.civilization.core.ai.movement;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum WalkPace implements StringIdentifiable {
    SNEAKY(0.4625d),
    SLOW(0.6d),
    DEFAULT(0.85d),
    RUN(1d),
    RUN_JUMP(1d);

    private final double speed;

    WalkPace(double speed) {
        this.speed = speed;
    }

    public static WalkPace fromString(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public double getSpeed() {
        return this.speed;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
