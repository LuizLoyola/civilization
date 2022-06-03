package br.com.tiozinnub.civilization.utils;

public class MapPos {
    private final int x;
    private final int z;

    public static MapPos ZERO = new MapPos(0, 0);

    public MapPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public MapPos add(MapPos other) {
        return new MapPos(x + other.x, z + other.z);
    }

    public MapPos move(CardinalDirection direction, int amount) {
        return direction.move(this, amount);
    }

    public MapPos north() {
        return north(1);
    }

    public MapPos north(int amount) {
        return new MapPos(x, z - amount);
    }

    public MapPos south() {
        return south(1);
    }

    public MapPos south(int amount) {
        return new MapPos(x, z + amount);
    }

    public MapPos east() {
        return east(1);
    }

    public MapPos east(int amount) {
        return new MapPos(x + amount, z);
    }

    public MapPos west() {
        return west(1);
    }

    public MapPos west(int amount) {
        return new MapPos(x - amount, z);
    }

    public long asLong() {
        return asLong(this.getX(), this.getZ());
    }

    public static long asLong(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) << 32 | (z & 0xFFFFFFFFL);
    }

    public static MapPos fromLong(long packedPos) {
        return new MapPos(unpackLongX(packedPos), unpackLongZ(packedPos));
    }

    public static int unpackLongX(long packedPos) {
        return (int) (packedPos >> 32);
    }

    public static int unpackLongZ(long packedPos) {
        return (int) packedPos;
    }

    @Override
    public String toString() {
        return "MapPos{x=%d, z=%d}".formatted(x, z);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapPos && ((MapPos) obj).x == x && ((MapPos) obj).z == z;
    }

    public CardinalDirection getDirection(MapPos other) {
        if (this == other) return null;

        if (this.getX() == other.getX())
            if (this.getZ() > other.getZ())
                return CardinalDirection.NORTH;
            else
                return CardinalDirection.SOUTH;
        else if (this.getZ() == other.getZ())
            if (this.getX() > other.getX())
                return CardinalDirection.EAST;
            else
                return CardinalDirection.WEST;
        else
            return null;
    }
}
