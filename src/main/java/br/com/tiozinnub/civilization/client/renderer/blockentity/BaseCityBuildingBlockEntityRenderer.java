package br.com.tiozinnub.civilization.client.renderer.blockentity;

import br.com.tiozinnub.civilization.block.entity.CityBuildingBlockEntity;
import br.com.tiozinnub.civilization.client.model.blockentity.BaseCityBuildingBlockEntityModel;

public class BaseCityBuildingBlockEntityRenderer<BE extends CityBuildingBlockEntity> extends CityBuildingBlockEntityRenderer<BE, BaseCityBuildingBlockEntityModel<BE>> {
    public BaseCityBuildingBlockEntityRenderer() {
        this("city_building_block");
    }

    public BaseCityBuildingBlockEntityRenderer(String blockName) {
        this(new BaseCityBuildingBlockEntityModel<>(blockName));
    }

    public BaseCityBuildingBlockEntityRenderer(BaseCityBuildingBlockEntityModel<BE> model) {
        super(model);
    }
}
