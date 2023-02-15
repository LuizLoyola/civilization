package br.com.tiozinnub.civilization.block.building.citycenter;

import br.com.tiozinnub.civilization.block.BlockEntities;
import br.com.tiozinnub.civilization.block.building.CityBuildingBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CityCenterBlockEntity extends CityBuildingBlockEntity<CityCenterBlockData> {
    public CityCenterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CITY_CENTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected CityCenterBlockData initializeBuildingData(Random random) {
        return new CityCenterBlockData(random);
    }

    @Override
    protected CityCenterBlockData initializeBuildingData(NbtCompound nbt) {
        return new CityCenterBlockData(nbt);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return super.toInitialChunkDataNbt();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        player.sendMessage(Text.of(getData().getName()), false);

        return ActionResult.SUCCESS;
    }
}
