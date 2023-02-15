package br.com.tiozinnub.civilization.block;

import br.com.tiozinnub.civilization.block.entity.CityCenterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CityCenterBlock extends CityBuildingBlock {
    public CityCenterBlock() {
        super();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CityCenterBlockEntity(pos, state);
    }
}
