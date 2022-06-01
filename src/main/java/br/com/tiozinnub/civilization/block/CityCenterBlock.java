package br.com.tiozinnub.civilization.block;

import br.com.tiozinnub.civilization.block.entity.CityCenterBlockEntity;
import br.com.tiozinnub.civilization.registry.ItemGroupRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class CityCenterBlock extends BlockBase implements BlockEntityProvider {
    public CityCenterBlock() {
        super(FabricBlockSettings.of(Material.WOOL));
    }

    @Override
    public Identifier getIdentifier() {
        return idFor("city_center_block");
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroupRegistry.SPECIAL_BLOCKS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CityCenterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockEntityProvider.super.getTicker(world, state, type);
    }
}
