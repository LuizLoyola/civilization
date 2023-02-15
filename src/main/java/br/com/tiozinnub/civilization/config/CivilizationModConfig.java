package br.com.tiozinnub.civilization.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

import static br.com.tiozinnub.civilization.utils.Constraints.MOD_ID;

@Config(name = MOD_ID)
public class CivilizationModConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.TransitiveObject
    MainConfig main = new MainConfig();

    @ConfigEntry.Category("person_names")
    @ConfigEntry.Gui.TransitiveObject
    PersonNamesConfig personNames = new PersonNamesConfig();

    @ConfigEntry.Category("city_names")
    @ConfigEntry.Gui.TransitiveObject
    CityNamesConfig cityNames = new CityNamesConfig();


    private static CivilizationModConfig getConfig() {
        return AutoConfig.getConfigHolder(CivilizationModConfig.class).getConfig();
    }

    public static MainConfig getMainConfig() {
        return getConfig().main;
    }

    public static PersonNamesConfig getPersonNamesConfig() {
        return getConfig().personNames;
    }

    public static CityNamesConfig getCityNamesConfig() {
        return getConfig().cityNames;
    }


}
