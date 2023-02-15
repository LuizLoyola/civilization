package br.com.tiozinnub.civilization.block.building;

import br.com.tiozinnub.civilization.utils.Serializable;

public abstract class CityBuildingBlockData extends Serializable {
    @Override
    public void registerProperties(SerializableHelper helper) {
        registerBlockDataProperties(helper);
    }

    protected abstract void registerBlockDataProperties(SerializableHelper helper);
}
