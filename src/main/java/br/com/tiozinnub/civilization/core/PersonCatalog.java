package br.com.tiozinnub.civilization.core;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.Map;
import java.util.UUID;

import static br.com.tiozinnub.civilization.utils.helper.NbtHelper.getHashMapWithInteger;
import static br.com.tiozinnub.civilization.utils.helper.NbtHelper.putHashMapWithInteger;

public class PersonCatalog extends PersistentState {
    private final Map<UUID, Integer> peopleMap = Maps.newHashMap();

    private final ServerWorld serverWorld;

    public PersonCatalog(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
        this.markDirty();
    }

    public static PersonCatalog fromNbt(ServerWorld serverWorld, NbtCompound nbt) {
        var personCatalog = new PersonCatalog(serverWorld);
        getHashMapWithInteger(nbt, "peopleMap", UUID::fromString, personCatalog.peopleMap);
        return personCatalog;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        putHashMapWithInteger(nbt, "peopleMap", this.peopleMap);
        return nbt;
    }

    public void update(Integer id, UUID uuid) {
        this.peopleMap.put(uuid, id);
        this.markDirty();
    }

    public ServerWorld getServerWorld() {
        return serverWorld;
    }

    public Integer getPersonId(UUID uuid) {
        return this.peopleMap.get(uuid);
    }

    public void remove(UUID uuid) {
        this.peopleMap.remove(uuid);
        this.markDirty();
    }

    public Map<UUID, Integer> getPeopleMap() {
        return peopleMap;
    }

    public void reset() {
        this.peopleMap.clear();
        this.markDirty();
    }
}
