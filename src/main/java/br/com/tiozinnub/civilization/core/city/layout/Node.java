package br.com.tiozinnub.civilization.core.city.layout;

import br.com.tiozinnub.civilization.utils.Serializable;

public class Node extends Serializable {
    private int x;
    private int y;

    private boolean hasNorthNeighbor;
    private boolean hasSouthNeighbor;
    private boolean hasWestNeighbor;
    private boolean hasEastNeighbor;

    private byte getNeighbors() {
        byte result = 0;
        if (hasNorthNeighbor) result |= 1;
        if (hasSouthNeighbor) result |= 2;
        if (hasWestNeighbor) result |= 4;
        if (hasEastNeighbor) result |= 8;
        return result;
    }

    private void setNeighbors(byte value) {
        hasNorthNeighbor = (value & 1) != 0;
        hasSouthNeighbor = (value & 2) != 0;
        hasWestNeighbor = (value & 4) != 0;
        hasEastNeighbor = (value & 8) != 0;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("x", this::getX, this::setX, 0);
        helper.registerProperty("y", this::getY, this::setY, 0);
        helper.registerProperty("neighbors", this::getNeighbors, this::setNeighbors, (byte) 0);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
