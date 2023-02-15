package br.com.tiozinnub.civilization.client.renderer.entity;

import br.com.tiozinnub.civilization.client.renderer.entity.model.PersonEntityModel;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

import static br.com.tiozinnub.civilization.util.Constraints.idFor;

@Environment(EnvType.CLIENT)
public class PersonEntityRenderer extends BipedEntityRenderer<PersonEntity, PersonEntityModel> {
    private static final Identifier TEXTURE = idFor("textures/entity/person/person.png");

    public PersonEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PersonEntityModel(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
        this.addFeature(new ArmorFeatureRenderer<>(this, new PersonEntityModel(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)), new PersonEntityModel(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR))));
    }

    @Override
    public Identifier getTexture(PersonEntity entity) {
        return TEXTURE;
    }
}
