package br.com.tiozinnub.civilization.entity;

import br.com.tiozinnub.civilization.client.renderer.entity.PersonEntityRenderer;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class Entities {
    public static final EntityType<PersonEntity> PERSON = buildEntity(PersonEntity::new, EntityDimensions.changing(0.6F, 1.8F), SpawnGroup.MISC, "person");

    private static <T extends Entity> EntityType<T> buildEntity(EntityType.EntityFactory<T> factory, EntityDimensions dimensions, SpawnGroup spawnGroup, String name) {
        return Registry.register(Registries.ENTITY_TYPE, idFor(name), FabricEntityTypeBuilder.create(spawnGroup, factory).dimensions(dimensions).build());
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(Entities.PERSON, PersonEntity.createPersonAttributes());
    }

    public static void registerClient() {
        EntityRendererRegistry.register(Entities.PERSON, PersonEntityRenderer::new);
    }
}
