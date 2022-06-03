package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.core.City;
import br.com.tiozinnub.civilization.utils.MapPos;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class Node extends CityLayoutPart {
    private MapPos mapPos;
    private boolean hasNorthNeighbor;
    private boolean hasSouthNeighbor;
    private boolean hasWestNeighbor;
    private boolean hasEastNeighbor;
    private BlockPos realPosition;

    public Node(City city) {
        super(city);
    }

    public Node(City city, MapPos mapPos) {
        this(city);
        this.mapPos = mapPos;
        this.realPosition = this.getCity().getRealPosFor(mapPos);
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
        helper.registerProperty("realPosition", this::getRealPos, p -> this.realPosition = p, new BlockPos(0, 0, 0));
    }

    public MapPos getPos() {
        return this.mapPos;
    }

    @Override
    public Box getBox() {
        var radius = getLayoutDimensions().getNodeRadius();
        return new Box(getRealPos().add(-radius, 0, -radius), getRealPos().add(radius, 0, radius));
    }

    @Override
    public Text getDescription() {
        return Text.of("Node %d,%d".formatted(mapPos.getX(), mapPos.getZ()));
    }

    public BlockPos getRealPos() {
        return realPosition;
    }
}
