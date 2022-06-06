package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.MapPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.EnumSet;

public class Lot {
    private final CityBlock cityBlock;
    private final int x;
    private final int z;
    private final BlockPos realPos1;
    private final BlockPos realPos2;
    private final EnumSet<LotFeature> features;
    private final EnumSet<CardinalDirection> streetDirections;

    public static final int LOT_SIZE = 7;
    public static final int INNER_MARGIN = 1;
    public static final int OUTER_MARGIN = 1;

    public Box getBox() {
        return new Box(this.realPos1, this.realPos2).withMaxY(realPos1.getY() + 5);
    }

    public Lot(CityBlock cityBlock, int x, int z) {
        this.cityBlock = cityBlock;

        this.features = EnumSet.noneOf(LotFeature.class);
        var cityBlockWidth = cityBlock.getSizeX() * 3 - 1;
        var cityBlockHeight = cityBlock.getSizeZ() * 3 - 1;

        if (x < 0 || x >= cityBlockWidth || z < 0 || z >= cityBlockHeight) {
            throw new IllegalArgumentException("Invalid lot position");
        }

        this.x = x;
        this.z = z;

        this.realPos1 = cityBlock.getRealPos1().add(OUTER_MARGIN + (x * (INNER_MARGIN + LOT_SIZE)), 1, OUTER_MARGIN + (z * (INNER_MARGIN + LOT_SIZE)));
        this.realPos2 = realPos1.add(LOT_SIZE - 1, 0, LOT_SIZE - 1);

        // check features
        streetDirections = EnumSet.noneOf(CardinalDirection.class);
        if (x == 0) {
            streetDirections.add(CardinalDirection.WEST);
        }
        if (x == cityBlockWidth - 1) {
            streetDirections.add(CardinalDirection.EAST);
        }
        if (z == 0) {
            streetDirections.add(CardinalDirection.NORTH);
        }
        if (z == cityBlockHeight - 1) {
            streetDirections.add(CardinalDirection.SOUTH);
        }
        if (streetDirections.size() > 0) {
            features.add(LotFeature.FACING_STREET);

            if (streetDirections.size() > 1) {
                features.add(LotFeature.CORNER);
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public MapPos getPos() {
        return new MapPos(x, z);
    }

    public BlockPos getRealPos1() {
        return realPos1;
    }

    public BlockPos getRealPos2() {
        return realPos2;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Lot lot && lot.getX() == this.getX() && lot.getZ() == this.getZ();
    }

    public boolean hasFeature(LotFeature feature) {
        return this.features.contains(feature);
    }

    public boolean isFacingStreet() {
        return this.features.contains(LotFeature.FACING_STREET);
    }

    public EnumSet<CardinalDirection> getStreetDirections() {
        return this.streetDirections;
    }

    public CityBlock getCityBlock() {
        return cityBlock;
    }

    public enum LotFeature {
        FACING_STREET,
        CORNER,
    }
}
