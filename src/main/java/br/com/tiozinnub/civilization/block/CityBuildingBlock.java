package br.com.tiozinnub.civilization.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;

public abstract class CityBuildingBlock extends BlockWithEntity {
    protected CityBuildingBlock() {
        this(Material.STONE);
    }

    public CityBuildingBlock(Material material) {
        super(FabricBlockSettings.of(material).strength(-1.0f).nonOpaque());

        setDefaultState(getStateManager().getDefaultState());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
