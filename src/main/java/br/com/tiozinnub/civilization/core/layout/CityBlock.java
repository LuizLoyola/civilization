package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.core.City;
import br.com.tiozinnub.civilization.utils.MapPos;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CityBlock extends CityLayoutPart {
    private MapPos pos1;
    private MapPos pos2;
    private int sizeX;
    private int sizeZ;
    private BlockPos realPos1;
    private BlockPos realPos2;

    public CityBlock(City city) {
        super(city);
    }

    public CityBlock(City city, MapPos pos1, MapPos pos2) {
        super(city);
        this.pos1 = pos1;
        this.pos2 = pos2;

        this.sizeX = Math.abs(pos1.getX() - pos2.getX());
        this.sizeZ = Math.abs(pos1.getZ() - pos2.getZ());

        var realPos1 = this.getCity().getRealPosFor(pos1);
        var realPos2 = this.getCity().getRealPosFor(pos2);

        var minRealPos = new BlockPos(Math.min(realPos1.getX(), realPos2.getX()), realPos1.getY(), Math.min(realPos1.getZ(), realPos2.getZ()));
        var maxRealPos = new BlockPos(Math.max(realPos1.getX(), realPos2.getX()), realPos1.getY(), Math.max(realPos1.getZ(), realPos2.getZ()));

        var margin = this.getLayoutDimensions().getNodeRadius() + 1;

        this.realPos1 = minRealPos.add(margin, 0, margin);
        this.realPos2 = maxRealPos.add(-margin, 0, -margin);
    }

    @Override
    public Box getBox() {
        return new Box(this.realPos1, this.realPos2);
    }

    @Override
    public Text getDescription() {
        return Text.of("%dx%d city block from %d,%d to %d,%d".formatted(
                this.sizeX,
                this.sizeZ,
                this.pos1.getX(),
                this.pos1.getZ(),
                this.pos2.getX(),
                this.pos2.getZ()
        ));
    }

    public MapPos getPos1() {
        return pos1;
    }

    public MapPos getPos2() {
        return pos2;
    }

    public BlockPos getRealPos1() {
        return realPos1;
    }

    public BlockPos getRealPos2() {
        return realPos2;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("pos1", this::getPos1, p -> this.pos1 = p, MapPos.ZERO);
        helper.registerProperty("pos2", this::getPos2, p -> this.pos2 = p, MapPos.ZERO);
        helper.registerProperty("realPos1", this::getRealPos1, p -> this.realPos1 = p, new BlockPos(0, 0, 0));
        helper.registerProperty("realPos2", this::getRealPos2, p -> this.realPos2 = p, new BlockPos(0, 0, 0));
        helper.registerProperty("sizeX", this::getSizeX, p -> this.sizeX = p, 0);
        helper.registerProperty("sizeZ", this::getSizeZ, p -> this.sizeZ = p, 0);
    }

    public int getMaxLotWidth() {
        return this.getSizeX() * 3 - 1;
    }

    public int getMaxLotHeight() {
        return this.getSizeZ() * 3 - 1;
    }
}
