package br.com.tiozinnub.civilization.core.ai.pathfinder;

public enum WalkPace {
    SNEAKY(0.4625d),
    SLOW(0.6d),
    DEFAULT(0.85d),
    RUN(1d),
    RUN_JUMP(1d);

    private final double speed;

    WalkPace(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return this.speed;
    }
}
