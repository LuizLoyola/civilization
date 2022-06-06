package br.com.tiozinnub.civilization.block;

import br.com.tiozinnub.civilization.block.entity.BlueprintTableBlockEntity;
import br.com.tiozinnub.civilization.registry.ItemGroupRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlueprintTableBlock extends BlockBase implements BlockEntityProvider {
    public BlueprintTableBlock() {
        super(FabricBlockSettings.of(Material.WOOD));
    }

    @Override
    public Identifier getIdentifier() {
        return idFor("blueprint_table_block");
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroupRegistry.SPECIAL_BLOCKS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlueprintTableBlockEntity(pos, state);
    }
}
