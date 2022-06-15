package br.com.tiozinnub.civilization.core;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static br.com.tiozinnub.civilization.utils.helper.NbtHelper.get;
import static br.com.tiozinnub.civilization.utils.helper.NbtHelper.put;

public class CityManager extends PersistentState {
    private final Map<UUID, City> cities = Maps.newHashMap();
    private final ServerWorld world;
    private int currentTime;

    public CityManager(ServerWorld world) {
        this.world = world;
        this.markDirty();
    }

    public City getCity(UUID uuid) {
        return this.cities.get(uuid);
    }

    public City getCity(String name, boolean caseSensitive) {
        return this.cities.values().stream().filter(city -> caseSensitive ? city.getName().equals(name) : city.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void tick() {
        ++this.currentTime;

        for (Iterator<City> iterator = this.cities.values().iterator(); iterator.hasNext(); ) {
            City city = iterator.next();

            if (city.shouldDelete()) {
                iterator.remove();
            } else {
                city.tick();
            }
        }

        this.markDirty();
    }

    public static CityManager fromNbt(ServerWorld world, NbtCompound nbt) {
        CityManager cityManager = new CityManager(world);
        cityManager.currentTime = get(nbt, "currentTime", 0);
        get(nbt, "cities", UUID::fromString, (c) -> new City(world, c), cityManager.cities);
        return cityManager;
    }

    public static String nameFor() {
        return "cityManager";
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        put(nbt, "currentTime", this.currentTime);
        put(nbt, "cities", this.cities);
        return nbt;
    }

    public City createCity(BlockPos pos, String name) {
        var city = this.getCity(name, false);
        if (city != null) {
            throw new IllegalArgumentException("City with name '" + city.getName() + "' already exists.");
        }

        city = new City(this.world, pos, name);
        this.cities.put(city.getCityId(), city);
        return city;
    }

    public City getCityAt(BlockPos pos) {
        for (City city : this.cities.values()) {
            if (city.isPosWithinCityChunks(pos)) {
                return city;
            }
        }

        return null;
    }

    public List<City> getCities() {
        return this.cities.values().stream().toList();
    }
}