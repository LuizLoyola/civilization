package br.com.tiozinnub.civilization.registry;

import br.com.tiozinnub.civilization.block.entity.BlueprintTableBlockEntity;
import br.com.tiozinnub.civilization.block.entity.CityCenterBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import static br.com.tiozinnub.civilization.registry.BlockRegistry.BLUEPRINT_TABLE_BLOCK;
import static br.com.tiozinnub.civilization.registry.BlockRegistry.CITY_CENTER_BLOCK;
import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlockEntityRegistry {
    public static BlockEntityType<BlueprintTableBlockEntity> BLUEPRINT_TABLE;
    public static BlockEntityType<CityCenterBlockEntity> CITY_CENTER;

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, idFor(name), FabricBlockEntityTypeBuilder.create(factory, blocks).build(null));
    }

    public static void register() {
        CITY_CENTER = registerBlockEntity("city_center_block_entity", CityCenterBlockEntity::new, CITY_CENTER_BLOCK);
        BLUEPRINT_TABLE = registerBlockEntity("blueprint_table_block_entity", BlueprintTableBlockEntity::new, BLUEPRINT_TABLE_BLOCK);
    }
}
