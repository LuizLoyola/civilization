package br.com.tiozinnub.civilization.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

public class ConfigRegistry {
    public static void register() {
        AutoConfig.register(CivilizationModConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
    }
}
