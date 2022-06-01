package br.com.tiozinnub.civilization.client.model.entity;

import br.com.tiozinnub.civilization.entity.EntityBase;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public abstract class EntityModelBase<T extends EntityBase> extends AnimatedGeoModel<T> {
    @Override
    public Identifier getModelLocation(T entity) {
        return idFor("geo/%s.geo.json".formatted(entity.getNameId()));
    }

    @Override
    public Identifier getTextureLocation(T entity) {
        return idFor("textures/entity/%s.png".formatted(entity.getNameId()));
    }

    @Override
    public Identifier getAnimationFileLocation(T entity) {
        return idFor("animations/%s.animation.json".formatted(entity.getNameId()));
    }
}
