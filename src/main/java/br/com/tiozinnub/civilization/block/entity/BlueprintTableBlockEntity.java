package br.com.tiozinnub.civilization.block.entity;

import br.com.tiozinnub.civilization.registry.BlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlueprintTableBlockEntity extends BlockEntityBase {
    public BlueprintTableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.BLUEPRINT_TABLE, pos, state);
    }
}
