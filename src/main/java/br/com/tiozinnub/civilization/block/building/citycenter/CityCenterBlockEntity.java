package br.com.tiozinnub.civilization.block.building.citycenter;

import br.com.tiozinnub.civilization.block.BlockEntityRegistry;
import br.com.tiozinnub.civilization.block.building.CityBuildingBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CityCenterBlockEntity extends CityBuildingBlockEntity<CityCenterBlockData> {
    public CityCenterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CITY_CENTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected CityCenterBlockData initializeBuildingData() {
        return CityCenterBlockData.randomize(getWorld().getRandom());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        player.sendMessage(Text.of(getData().getName()), false);

        return ActionResult.SUCCESS;
    }
}
