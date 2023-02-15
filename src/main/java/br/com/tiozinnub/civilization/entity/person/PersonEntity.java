package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.EntityBase;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class PersonEntity extends EntityBase {
    public static final EntityDimensions DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F);
    public static final SpawnGroup SPAWN_GROUP = SpawnGroup.MISC;
    public static final String NAME = "person";

    public PersonEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return EntityBase.createMobAttributes(); // TODO
    }
}
