package br.com.tiozinnub.civilization.client.model.blockentity;

import br.com.tiozinnub.civilization.block.entity.CityBuildingBlockEntity;
import net.minecraft.util.Identifier;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BaseCityBuildingBlockEntityModel<BE extends CityBuildingBlockEntity> extends CityBuildingBlockEntityModel<BE> {
    private final String blockName;

    public BaseCityBuildingBlockEntityModel(String blockName) {
        super();

        this.blockName = blockName;
    }

    @Override
    public Identifier getModelResource(BE cityBuildingBlockEntity) {
        return idFor("geo/block_entity/%s.geo.json".formatted(blockName));
    }

    @Override
    public Identifier getTextureResource(BE cityBuildingBlockEntity) {
        return idFor("textures/block/%s.png".formatted(blockName));
    }

    @Override
    public Identifier getAnimationResource(BE cityBuildingBlockEntity) {
        return null;
    }
}
