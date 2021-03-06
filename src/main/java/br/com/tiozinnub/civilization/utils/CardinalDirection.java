package br.com.tiozinnub.civilization.utils;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;

public enum CardinalDirection implements StringIdentifiable {
    NORTH("north", Direction.Axis.Z),
    SOUTH("south", Direction.Axis.Z),
    EAST("east", Direction.Axis.X),
    WEST("west", Direction.Axis.X);

    private final String name;
    private final Direction.Axis axis;

    public static final EnumSet<CardinalDirection> NORTH_EAST = EnumSet.of(NORTH, EAST);
    public static final EnumSet<CardinalDirection> NORTH_WEST = EnumSet.of(NORTH, WEST);
    public static final EnumSet<CardinalDirection> SOUTH_EAST = EnumSet.of(SOUTH, EAST);
    public static final EnumSet<CardinalDirection> SOUTH_WEST = EnumSet.of(SOUTH, WEST);
    public static final EnumSet<CardinalDirection> ALL = EnumSet.allOf(CardinalDirection.class);

    CardinalDirection(String name, Direction.Axis axis) {
        this.name = name;
        this.axis = axis;
    }

    @Nullable
    public static CardinalDirection byName(@Nullable String name) {
        return Arrays.stream(values()).filter(e -> e.asString().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static CardinalDirection fromDirection(Direction side) {
        return switch (side) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> null;
        };
    }

    public Direction toDirection() {
        return switch (this) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
        };
    }

    public String getName() {
        return this.name;
    }

    public Direction.Axis getAxis() {
        return axis;
    }

    @Override
    public String asString() {
        return name;
    }

    public BlockPos move(BlockPos pos) {
        return move(pos, 1);
    }

    public BlockPos move(BlockPos pos, int amount) {
        return switch (this) {
            case NORTH -> pos.north(amount);
            case SOUTH -> pos.south(amount);
            case EAST -> pos.east(amount);
            case WEST -> pos.west(amount);
        };
    }

    public CardinalDirection opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    public CardinalDirection left() {
        return switch (this) {
            case NORTH -> WEST;
            case SOUTH -> EAST;
            case EAST -> NORTH;
            case WEST -> SOUTH;
        };
    }

    public CardinalDirection left(int amount) {
        return switch (amount % 4) {
            case 1 -> left();
            case 2 -> opposite();
            case 3 -> right();
            default -> this;
        };
    }

    public CardinalDirection right() {
        return switch (this) {
            case NORTH -> EAST;
            case SOUTH -> WEST;
            case EAST -> SOUTH;
            case WEST -> NORTH;
        };
    }

    public CardinalDirection right(int amount) {
        return switch (amount % 4) {
            case 1 -> right();
            case 2 -> opposite();
            case 3 -> left();
            default -> this;
        };
    }

    public Direction asDirection() {
        return switch (this) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
        };
    }
}
