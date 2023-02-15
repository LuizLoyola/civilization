package br.com.tiozinnub.civilization.block.building;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class CityBuildingBlock extends BlockWithEntity {
    protected CityBuildingBlock() {
        this(Material.STONE);
    }

    public CityBuildingBlock(Material material) {
        super(FabricBlockSettings.of(material).strength(-1.0f).nonOpaque());

        setDefaultState(getStateManager().getDefaultState());
    }

    @Override
    public final BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof CityBuildingBlockEntity<?> blockEntity)) return ActionResult.SUCCESS;

        return blockEntity.onUse(state, world, pos, player, hand, hit);
    }
}
