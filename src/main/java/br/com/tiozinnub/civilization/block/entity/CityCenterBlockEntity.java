package br.com.tiozinnub.civilization.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CityCenterBlockEntity extends CityBuildingBlockEntity {
    public CityCenterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CITY_CENTER_BLOCK_ENTITY, pos, state);
    }
}
