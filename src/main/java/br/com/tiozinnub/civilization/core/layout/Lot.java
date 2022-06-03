package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.core.City;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class Lot extends CityLayoutPart {
    private int x;
    private int z;
    private int width;
    private int height;
    private BlockPos realPos1;
    private BlockPos realPos2;

    private static final int LOT_SIZE = 7;
    private static final int INNER_MARGIN = 1;
    private static final int OUTER_MARGIN = 1;

    public Lot(City city) {
        super(city);
    }

    @Override
    public Box getBox() {
        return new Box(this.realPos1, this.realPos2);
    }

    @Override
    public Text getDescription() {
        return Text.of("Lot at %d,%d (size %dx%d)".formatted(this.x,
                this.z,
                this.width,
                this.height
        ));
    }

    public Lot(City city, CityBlock cityBlock, int x, int z, int width, int height) {
        this(city);

        var cityBlockWidth = cityBlock.getSizeX() * 3 - 1;
        var cityBlockHeight = cityBlock.getSizeZ() * 3 - 1;

        if (x < 0 || x + width > cityBlockWidth) throw new IllegalArgumentException("x must be between 0 and " + cityBlockWidth);
        if (z < 0 || z + height > cityBlockHeight) throw new IllegalArgumentException("z must be between 0 and " + cityBlockHeight);
        if (width > cityBlockWidth - x) throw new IllegalArgumentException("width must be less than " + (cityBlockWidth - x));
        if (height > cityBlockHeight - z) throw new IllegalArgumentException("height must be less than " + (cityBlockHeight - z));

        this.x = x;
        this.z = z;
        this.width = width;
        this.height = height;

        this.realPos1 = cityBlock.getRealPos1().add(OUTER_MARGIN + (x * (INNER_MARGIN + LOT_SIZE)), 0, OUTER_MARGIN + (z * (INNER_MARGIN + LOT_SIZE)));
        this.realPos2 = realPos1.add(width * (LOT_SIZE + INNER_MARGIN), 0, height * (LOT_SIZE + INNER_MARGIN));
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BlockPos getRealPos1() {
        return realPos1;
    }

    public BlockPos getRealPos2() {
        return realPos2;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("x", this::getX, p -> this.x = p, 0);
        helper.registerProperty("z", this::getZ, p -> this.z = p, 0);
        helper.registerProperty("width", this::getWidth, p -> this.width = p, 0);
        helper.registerProperty("height", this::getHeight, p -> this.height = p, 0);
        helper.registerProperty("realPos1", this::getRealPos1, p -> this.realPos1 = p, null);
        helper.registerProperty("realPos2", this::getRealPos2, p -> this.realPos2 = p, null);
    }
}
