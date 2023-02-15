package br.com.tiozinnub.civilization.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class EntityBase extends MobEntity {
    protected EntityBase(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
}
