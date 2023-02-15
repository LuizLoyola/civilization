package br.com.tiozinnub.civilization.client.renderer.blockentity;

import br.com.tiozinnub.civilization.block.entity.CityBuildingBlockEntity;
import br.com.tiozinnub.civilization.client.model.blockentity.CityBuildingBlockEntityModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public abstract class CityBuildingBlockEntityRenderer<BE extends CityBuildingBlockEntity, M extends CityBuildingBlockEntityModel<BE>> extends GeoBlockRenderer<BE> {

    public CityBuildingBlockEntityRenderer(GeoModel<BE> model) {
        super(model);
    }
}
