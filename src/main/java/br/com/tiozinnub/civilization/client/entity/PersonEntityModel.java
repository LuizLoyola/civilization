package br.com.tiozinnub.civilization.client.entity;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

import java.util.Locale;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class PersonEntityModel extends GeoModel<PersonEntity> {
    @Override
    public Identifier getModelResource(PersonEntity personEntity) {
        return idFor("geo/entity/person/%s_%s.geo.json".formatted(
                personEntity.getSpecies().asString().toLowerCase(Locale.ROOT),
                personEntity.getGender().asString().toLowerCase(Locale.ROOT)
        ));
    }

    @Override
    public Identifier getTextureResource(PersonEntity personEntity) {
        return idFor("textures/entity/person/%s_%s.png".formatted(
                personEntity.getSpecies().asString().toLowerCase(Locale.ROOT),
                personEntity.getGender().asString().toLowerCase(Locale.ROOT)
        ));
    }

    @Override
    public Identifier getAnimationResource(PersonEntity personEntity) {
        return idFor("animations/entity/person/%s.animation.json".formatted(
                personEntity.getSpecies().asString().toLowerCase(Locale.ROOT)
        ));
    }
}
