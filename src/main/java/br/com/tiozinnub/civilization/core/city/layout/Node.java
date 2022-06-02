package br.com.tiozinnub.civilization.core.city.layout;

import br.com.tiozinnub.civilization.core.city.City;
import br.com.tiozinnub.civilization.core.city.LayoutDimensions;
import br.com.tiozinnub.civilization.utils.MapPos;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.firstBlockDown;

public class Node extends Serializable {
    private final City city;
    private MapPos mapPos;
    private boolean hasNorthNeighbor;
    private boolean hasSouthNeighbor;
    private boolean hasWestNeighbor;
    private boolean hasEastNeighbor;
    private BlockPos realPosition;

    public Node(City city) {
        this.city = city;
    }

    public Node(City city, MapPos mapPos) {
        this(city);
        this.mapPos = mapPos;
        var offset = this.city.getPositionOffset();

        var nodeDistance = getLayoutDimensions().streetWidth() + getLayoutDimensions().cityBlockSize();

        var posX = this.mapPos.getX() * nodeDistance + offset.getX();
        var posY = offset.getY();
        var posZ = this.mapPos.getZ() * nodeDistance + offset.getZ();
        posY = firstBlockDown(this.city.getWorld(), posX, posY, posZ).getY();

        this.realPosition = new BlockPos(posX, posY, posZ);
    }

    private byte getNeighborsByte() {
        byte result = 0;
        if (hasNorthNeighbor) result |= 1;
        if (hasSouthNeighbor) result |= 2;
        if (hasWestNeighbor) result |= 4;
        if (hasEastNeighbor) result |= 8;
        return result;
    }

    private void setNeighborsByte(byte value) {
        hasNorthNeighbor = (value & 1) != 0;
        hasSouthNeighbor = (value & 2) != 0;
        hasWestNeighbor = (value & 4) != 0;
        hasEastNeighbor = (value & 8) != 0;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("position", this::getPos, p -> this.mapPos = p, MapPos.ZERO);
        helper.registerProperty("neighbors", this::getNeighborsByte, this::setNeighborsByte, (byte) 0);
        helper.registerProperty("realPosition", this::getRealPosition, p -> this.realPosition = p, new BlockPos(0, 0, 0));
    }

    private LayoutDimensions getLayoutDimensions() {
        return this.city.getLayoutDimensions();
    }

    public MapPos getPos() {
        return this.mapPos;
    }

    public Box getBox() {
        var streetWidth = getLayoutDimensions().streetWidth();

        var radius = (streetWidth - 1) / 2;

        return new Box(getRealPosition().add(-radius, 0, -radius), getRealPosition().add(radius, 0, radius));
    }

    public BlockPos getRealPosition() {
        return realPosition;
    }

    public ChunkPos getChunk() {
        return new ChunkPos(getRealPosition());
    }
}
