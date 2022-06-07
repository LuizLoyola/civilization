package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlueprintMaker extends Serializable {
    public BlockPos firstPos;
    public BlockPos secondPos;

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("firstPos", () -> this.firstPos, (value) -> this.firstPos = value, null);
        helper.registerProperty("secondPos", () -> this.secondPos, (value) -> this.secondPos = value, null);
    }

    public Text usedOnBlock(World world, BlockPos blockPos, Direction side) {
        if (this.firstPos == null) {
            this.firstPos = blockPos;
            return Text.of("First position set. Click again to set second position.");
        } else if (this.secondPos == null) {
            this.secondPos = blockPos;
            return Text.of("Second position set.");
        } else {
            return Text.of("Positions already set.");
        }
    }

    public Text usedOnAir(ServerWorld world, boolean sneaking) {
        return null;
    }
}
