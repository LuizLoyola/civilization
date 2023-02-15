package br.com.tiozinnub.civilization.client.renderer.entity;

import br.com.tiozinnub.civilization.client.entity.PersonEntityModel;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PersonEntityRenderer extends GeoEntityRenderer<PersonEntity> {
    public PersonEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PersonEntityModel());
    }
}
