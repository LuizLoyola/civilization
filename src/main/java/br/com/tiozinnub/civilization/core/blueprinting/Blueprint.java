package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import net.minecraft.block.BlockState;

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
        //noinspection SuspiciousNameCombination
        var blueprint = new Blueprint(lengthZ, lengthX, lengthY);
        blueprint.direction = direction.right();
        for (var x = 0; x < lengthX; x++) {
            for (var y = 0; y < lengthY; y++) {
                for (var z = 0; z < lengthZ; z++) {
                    blueprint.blockStates[x][y][z] = blockStates[z][y][x];
                }
            }
        }
        return blueprint;
    }
}
