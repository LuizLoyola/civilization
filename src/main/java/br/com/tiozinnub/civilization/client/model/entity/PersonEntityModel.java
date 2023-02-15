package br.com.tiozinnub.civilization.client.model.entity;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

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
}
