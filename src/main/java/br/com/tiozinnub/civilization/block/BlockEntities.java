package br.com.tiozinnub.civilization.block;

import br.com.tiozinnub.civilization.block.building.citycenter.CityCenterBlockEntity;
import br.com.tiozinnub.civilization.client.renderer.blockentity.BaseCityBuildingBlockEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlockEntities {
    public static BlockEntityType<CityCenterBlockEntity> CITY_CENTER_BLOCK_ENTITY;

    public static void register() {
        CITY_CENTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, idFor("city_center_block_entity"), FabricBlockEntityTypeBuilder.create(CityCenterBlockEntity::new, Blocks.CITY_CENTER_BLOCK).build(null));
    }

    public static void registerClient() {
        BlockEntityRendererRegistry.register(CITY_CENTER_BLOCK_ENTITY, ctx -> new BaseCityBuildingBlockEntityRenderer<>());
    }
}
