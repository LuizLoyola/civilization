package br.com.tiozinnub.civilization.utils;

import net.minecraft.util.StringIdentifiable;

public enum CardinalDirection implements StringIdentifiable {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west");

    private final String name;

    CardinalDirection(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    public MapPos move(MapPos pos) {
        return move(pos, 1);
    }

    public MapPos move(MapPos pos, int amount) {
        return switch (this) {
            case NORTH -> pos.north(amount);
            case SOUTH -> pos.south(amount);
            case EAST -> pos.east(amount);
            case WEST -> pos.west(amount);
        };
    }
}
