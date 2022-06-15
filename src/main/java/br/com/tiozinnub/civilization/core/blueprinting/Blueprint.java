package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class Blueprint {
    private final int lengthX;
    private final int lengthY;
    private final int lengthZ;
    protected CardinalDirection direction;
    protected final BlockState[][][] blockStates;

    public Blueprint(int lengthX, int lengthY, int lengthZ) {
        this.lengthX = lengthX;
        this.lengthY = lengthY;
        this.lengthZ = lengthZ;
        blockStates = new BlockState[lengthX][lengthY][lengthZ];
    }

    public int getLengthX() {
        return lengthX;
    }

    public int getLengthY() {
        return lengthY;
    }

    public int getLengthZ() {
        return lengthZ;
    }

    public Blueprint rotate(CardinalDirection direction, boolean mirror) {
        var newX = direction == this.direction || direction == this.direction.opposite() ? lengthX : lengthZ;
        var newZ = direction == this.direction || direction == this.direction.opposite() ? lengthZ : lengthX;

        var right = direction == this.direction.right();
        var left = direction == this.direction.left();
        var opposite = direction == this.direction.opposite();

        var newBlueprint = new Blueprint(newX, lengthY, newZ);

        for (var x = 0; x < newX; x++) {
            for (var y = 0; y < lengthY; y++) {
                for (var z = 0; z < newZ; z++) {
                    int xx = mirror ? newX - x - 1 : x;

                    BlockState b;

                    if (right) {
                        b = blockStates[z][y][newX - xx - 1];
                    } else if (left) {
                        b = blockStates[newZ - z - 1][y][xx];
                    } else if (opposite) {
                        b = blockStates[newX - xx - 1][y][newZ - z - 1];
                    } else {
                        b = blockStates[xx][y][z];
                    }

                    newBlueprint.blockStates[x][y][z] = rotateBlock(b, direction, mirror);
                }
            }
        }


        return newBlueprint;
    }

    private BlockState rotateBlock(BlockState blockState, CardinalDirection direction, boolean mirror) {
        if (direction == this.direction && !mirror) return blockState;
        var right = direction == this.direction.right();
        var left = direction == this.direction.left();
        var opposite = direction == this.direction.opposite();

        var properties = blockState.getProperties();
        for (var property : properties) {
            switch (property.getName()) {
                case "facing" -> {
                    var facingProperty = (DirectionProperty) property;
                    var facing = blockState.get(facingProperty);
                    if (facing.getAxis() == Direction.Axis.Y) continue;
                    if (right) facing = facing.rotateYClockwise();
                    if (left) facing = facing.rotateYCounterclockwise();
                    if (opposite) facing = facing.getOpposite();
                    blockState = blockState.with(facingProperty, facing);
                }
                case "axis" -> {
                    //noinspection unchecked
                    var axisProperty = (EnumProperty<Direction.Axis>) property;
                    var axis = blockState.get(axisProperty);
                    if (axis == Direction.Axis.Y) continue;
                    if (right || left) axis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
                    blockState = blockState.with(axisProperty, axis);
                }
            }
        }

        // check for north, south, east, west properties
        if (properties.containsAll(List.of(Properties.NORTH, Properties.SOUTH, Properties.EAST, Properties.WEST))) {
            var north = blockState.get(Properties.NORTH);
            var south = blockState.get(Properties.SOUTH);
            var east = blockState.get(Properties.EAST);
            var west = blockState.get(Properties.WEST);

            if (right) {
                var n = north;
                north = west;
                west = south;
                south = east;
                east = n;
            }
            if (left) {
                var n = north;
                north = east;
                east = south;
                south = west;
                west = n;
            }
            if (opposite) {
                var n = north;
                north = south;
                south = n;
                var e = east;
                east = west;
                west = e;
            }

            blockState = blockState.with(Properties.NORTH, north).with(Properties.SOUTH, south).with(Properties.EAST, east).with(Properties.WEST, west);
        }

        return blockState;
    }

    public BlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (x < 0 || x >= lengthX || y < 0 || y >= lengthY || z < 0 || z >= lengthZ) {
            throw new IllegalArgumentException("Invalid matrix position: " + x + ", " + y + ", " + z);
        }

        return blockStates[x][y][z];
    }

    public CardinalDirection getDirection() {
        return direction;
    }
}
