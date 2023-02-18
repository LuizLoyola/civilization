package br.com.tiozinnub.civilization.client.model.entity;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import java.util.Locale;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class PersonEntityModel extends GeoModel<PersonEntity> {
    @Override
    public Identifier getModelResource(PersonEntity personEntity) {
        var identity = personEntity.getIdentity();

        return idFor("geo/entity/person/%s_%s.geo.json".formatted(
                identity.getSpecies().asString().toLowerCase(Locale.ROOT),
                identity.getGender().asString().toLowerCase(Locale.ROOT)
        ));
    }

    @Override
    public Identifier getTextureResource(PersonEntity personEntity) {
        var identity = personEntity.getIdentity();

        return idFor("textures/entity/person/%s_%s.png".formatted(
                identity.getSpecies().asString().toLowerCase(Locale.ROOT),
                identity.getGender().asString().toLowerCase(Locale.ROOT)
        ));
    }


    @Override
    public Identifier getAnimationResource(PersonEntity personEntity) {
        var identity = personEntity.getIdentity();

        return idFor("animations/entity/person/%s.animation.json".formatted(
                identity.getSpecies().asString().toLowerCase(Locale.ROOT)
        ));
    }

    @Override
    public void setCustomAnimations(PersonEntity animatable, long instanceId, AnimationState<PersonEntity> animationState) {
        var head = getAnimationProcessor().getBone("head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

            head.setRotX(entityData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
        }
    }

    @Override
    public void applyMolangQueries(PersonEntity animatable, double animTime) {
        super.applyMolangQueries(animatable, animTime);

        var parser = MolangParser.INSTANCE;

        var velocity = animatable.getVelocity();
        var speed = MathHelper.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));

        try {
            parser.setMemoizedValue("variable.tcos0", () -> Math.cos(animTime / 3f) * speed * 250);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
