package br.com.tiozinnub.civilization.utils.helper;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static void put(NbtCompound nbt, String key, Boolean value) {
        nbt.putBoolean(key, value);
    }

    public static void put(NbtCompound nbt, String key, Long value) {
        nbt.putLong(key, value);
    }

    public static void put(NbtCompound nbt, String key, String value) {
        nbt.putString(key, value);
    }

    public static void put(NbtCompound nbt, String key, Byte value) {
        nbt.putByte(key, value);
    }

    public static void put(NbtCompound nbt, String key, NbtCompound value) {
        nbt.put(key, value);
    }

    public static void put(NbtCompound nbt, String key, BlockPos value) {
        nbt.putLong(key, value.asLong());
    }

    public static void put(NbtCompound nbt, String key, Box value) {
        var min = new BlockPos(value.minX, value.minY, value.minZ);
        var max = new BlockPos(value.maxX, value.maxY, value.maxZ);
        put(nbt, key + "_min", min);
        put(nbt, key + "_max", max);
    }

    public static <T extends StringIdentifiable> void put(NbtCompound nbt, String key, T value) {
        put(nbt, key, value.asString());
    }

    public static void put(NbtCompound nbt, String key, UUID value) {
        nbt.putUuid(key, value);
    }

    public static <T extends Serializable> void put(NbtCompound nbt, String key, T value) {
        nbt.put(key, value.toNbt());
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

    public static Integer get(NbtCompound nbt, String key, Integer defaultValue) {
        return nbt.contains(key) ? nbt.getInt(key) : defaultValue;
    }

    public static Boolean get(NbtCompound nbt, String key, Boolean defaultValue) {
        return nbt.contains(key) ? nbt.getBoolean(key) : defaultValue;
    }

    public static Long get(NbtCompound nbt, String key, Long defaultValue) {
        return nbt.contains(key) ? nbt.getLong(key) : defaultValue;
    }

    public static String get(NbtCompound nbt, String key, String defaultValue) {
        return nbt.contains(key) ? nbt.getString(key) : defaultValue;
    }

    public static Byte get(NbtCompound nbt, String key, Byte defaultValue) {
        return nbt.contains(key) ? nbt.getByte(key) : defaultValue;
    }

    public static NbtCompound get(NbtCompound nbt, String key, NbtCompound defaultValue) {
        return nbt.contains(key) ? nbt.getCompound(key) : defaultValue;
    }

    public static BlockPos get(NbtCompound nbt, String key, BlockPos defaultValue) {
        return nbt.contains(key) ? BlockPos.fromLong(nbt.getLong(key)) : defaultValue;
    }

    public static UUID get(NbtCompound nbt, String key, UUID defaultValue) {
        return nbt.contains(key) ? nbt.getUuid(key) : defaultValue;
    }

    public static <T extends StringIdentifiable> T get(NbtCompound nbt, String key, T defaultValue, Function<String, T> valueParser) {
        return nbt.contains(key) ? valueParser.apply(nbt.getString(key)) : defaultValue;
    }

    public static Box get(NbtCompound nbt, String key, Box defaultValue) {
        var min = get(nbt, key + "_min", (BlockPos) null);
        var max = get(nbt, key + "_max", (BlockPos) null);
        return min != null && max != null ? new Box(min, max) : defaultValue;
    }

    public static <T extends Serializable> Serializable get(NbtCompound nbt, String key, Supplier<? extends Serializable> valueConstructor) {
        var compound = nbt.getCompound(key);
        return valueConstructor.get().fromNbt(compound);
    }
}
