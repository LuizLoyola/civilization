package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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

    public Blueprint rotate(CardinalDirection direction) {
        var blueprint = this;
        while (blueprint.direction != direction) {
            blueprint = blueprint.rotateRight();
        }
        return blueprint;
    }

    private Blueprint rotateRight() {
        var blueprint = new Blueprint(lengthZ, lengthY, lengthX);
        blueprint.direction = direction.right();
        for (var x = 0; x < blueprint.lengthX; x++) {
            for (var y = 0; y < blueprint.lengthY; y++) {
                for (var z = 0; z < blueprint.lengthZ; z++) {
                    blueprint.blockStates[x][y][z] = rotateBlockStateRight(blockStates[z][y][x]);
                }
            }
        }
        return blueprint;
    }

    @SuppressWarnings("unchecked")
    private static BlockState rotateBlockStateRight(BlockState blockState) {
        var properties = blockState.getProperties();
        for (var property : properties) {
            switch (property.getName()) {
                case "facing" -> {
                    var facingProperty = (DirectionProperty) property;
                    var facing = blockState.get(facingProperty);
                    if (facing.getAxis() == Direction.Axis.Y) continue;
                    blockState = blockState.with(facingProperty, facing.rotateYClockwise());
                }
                case "axis" -> {
                    var axisProperty = (EnumProperty<Direction.Axis>) property;
                    var axis = blockState.get(axisProperty);
                    if (axis == Direction.Axis.Y) continue;
                    blockState = blockState.with(axisProperty, axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
                }
            }
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
