package br.com.tiozinnub.civilization.block.entity;

import br.com.tiozinnub.civilization.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CityCenterBlockEntity extends BlockEntityBase {

    public CityCenterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CITY_CENTER_BLOCK_ENTITY, pos, state);
    }
}
