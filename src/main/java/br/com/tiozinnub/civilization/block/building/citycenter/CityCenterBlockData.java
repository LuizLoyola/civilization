package br.com.tiozinnub.civilization.block.building.citycenter;

import br.com.tiozinnub.civilization.block.building.CityBuildingBlockData;
import net.minecraft.util.math.random.Random;

import static br.com.tiozinnub.civilization.config.CivilizationModConfig.getCityNamesConfig;

public class CityCenterBlockData extends CityBuildingBlockData {
    private String name;

    public static CityCenterBlockData randomize(Random random) {
        CityCenterBlockData data = new CityCenterBlockData();
        data.setName(getCityNamesConfig().getRandomName(random));
        return data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void registerBlockDataProperties(SerializableHelper helper) {
        helper.registerProperty("name", this::getName, this::setName, null);
    }
}
