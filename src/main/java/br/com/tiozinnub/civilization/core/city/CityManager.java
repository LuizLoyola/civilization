package br.com.tiozinnub.civilization.core.city;

import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class CityManager {
    private final List<City> cities;

    public CityManager(ServerWorld world) {

        this.cities = new ArrayList<>();
    }
}
