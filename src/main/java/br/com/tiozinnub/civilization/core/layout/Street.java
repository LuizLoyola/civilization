package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.core.City;
import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.MapPos;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Street extends CityLayoutPart {
    private CardinalDirection direction;
    private BlockPos realPos1;
    private BlockPos realPos2;
    private MapPos pos1;
    private MapPos pos2;

    public Street(City city) {
        super(city);
    }

    public Street(City city, MapPos pos1, MapPos pos2) {
        this(city);
        this.pos1 = pos1;
        this.pos2 = pos2;

        this.direction = pos1.getDirection(pos2);

        this.realPos1 = direction.move(this.getCity().getRealPosFor(pos1), this.getLayoutDimensions().getNodeRadius() + 1);
        this.realPos2 = direction.move(this.realPos1, this.getLayoutDimensions().getCityBlockSize() - 1);
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("direction", this::getDirection, p -> this.direction = p, CardinalDirection.NORTH);
        helper.registerProperty("pos1", this::getPos1, p -> this.pos1 = p, MapPos.ZERO);
        helper.registerProperty("pos2", this::getPos2, p -> this.pos2 = p, MapPos.ZERO);
        helper.registerProperty("realPos1", this::getRealPos1, p -> this.realPos1 = p, new BlockPos(0, 0, 0));
        helper.registerProperty("realPos2", this::getRealPos2, p -> this.realPos2 = p, new BlockPos(0, 0, 0));
    }

    public MapPos getPos1() {
        return pos1;
    }

    public MapPos getPos2() {
        return pos2;
    }

    public CardinalDirection getDirection() {
        return direction;
    }

    public BlockPos getRealPos1() {
        return realPos1;
    }

    public BlockPos getRealPos2() {
        return realPos2;
    }

    @Override
    public Box getBox() {
        var radius = getLayoutDimensions().getNodeRadius();
        return new Box(direction.left().move(realPos1, radius), direction.right().move(realPos2, radius));
    }

    @Override
    public Text getDescription() {
        return Text.of("%s street from %d,%d %s to %d,%d".formatted(direction.getAxis() == Direction.Axis.Z ? "Vertical" : "Horizontal",
                pos1.getX(),
                pos1.getZ(),
                direction.asString(),
                pos2.getX(),
                pos2.getZ()
        ));
    }
}
