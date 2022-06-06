package br.com.tiozinnub.civilization.utils.helper;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NbtHelper {
    public static <K, V extends Serializable> void put(NbtCompound nbt, String key, Map<K, V> map) {
        var compound = new NbtCompound();
        for (var entry : map.entrySet()) {
            var entryKey = entry.getKey();
            var entryValue = entry.getValue();
            var entryNbt = entryValue.toNbt();
            compound.put(entryKey.toString(), entryNbt);
        }
        nbt.put(key, compound);
    }

    public static void put(NbtCompound nbt, String key, Integer value) {
        nbt.putInt(key, value);
    }

    public static <K, V extends Serializable> Map<K, V> get(NbtCompound nbt, String key, Function<String, K> keyConstructor, Function<NbtCompound, V> valueConstructor) {
        var map = new HashMap<K, V>();
        get(nbt, key, keyConstructor, valueConstructor, map);
        return map;
    }

    public static <K, V extends Serializable> void get(NbtCompound nbt, String key, Function<String, K> keyConstructor, Function<NbtCompound, V> valueConstructor, Map<K, V> map) {
        var compound = nbt.getCompound(key);
        for (var entry : compound.getKeys()) {
            var entryKey = keyConstructor.apply(entry);
            var entryValue = valueConstructor.apply(compound.getCompound(entry));
            map.put(entryKey, entryValue);
        }
    }

    public static Integer get(NbtCompound nbt, String key) {
        return nbt.getInt(key);
    }

    public static <T extends Serializable> T get(NbtCompound nbt, String key, Function<NbtCompound, T> valueConstructor) {
        var compound = nbt.getCompound(key);
        return valueConstructor.apply(compound);
    }
}
