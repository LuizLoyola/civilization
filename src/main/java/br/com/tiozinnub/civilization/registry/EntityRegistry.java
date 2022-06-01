package br.com.tiozinnub.civilization.registry;

import br.com.tiozinnub.civilization.client.renderer.entity.PersonEntityRenderer;
import br.com.tiozinnub.civilization.entity.property.Gender;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import software.bernie.example.registry.EntityRegistryBuilder;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class EntityRegistry {
    public static final EntityType<PersonEntity> PERSON_MALE = buildEntity((type, world) -> new PersonEntity(type, world, Gender.MALE), PersonEntity.DIMENSIONS, PersonEntity.SPAWN_GROUP, PersonEntity.NAME_MALE);
    public static final EntityType<PersonEntity> PERSON_FEMALE = buildEntity((type, world) -> new PersonEntity(type, world, Gender.FEMALE), PersonEntity.DIMENSIONS, PersonEntity.SPAWN_GROUP, PersonEntity.NAME_FEMALE);

    private static <T extends Entity> EntityType<T> buildEntity(EntityType.EntityFactory<T> entity, EntityDimensions dimensions, SpawnGroup group, String name) {
        return EntityRegistryBuilder
                .<T>createBuilder(idFor(name))
                .entity(entity)
                .category(group)
                .dimensions(dimensions)
                .build();
    }

    public static void register() {
        //noinspection ConstantConditions
        FabricDefaultAttributeRegistry.register(EntityRegistry.PERSON_MALE, PersonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(EntityRegistry.PERSON_FEMALE, PersonEntity.createAttributes());
    }

    public static void registerClient() {
        EntityRendererRegistry.register(EntityRegistry.PERSON_MALE, ctx -> new PersonEntityRenderer(ctx, Gender.MALE));
        EntityRendererRegistry.register(EntityRegistry.PERSON_FEMALE, ctx -> new PersonEntityRenderer(ctx, Gender.FEMALE));
    }
}