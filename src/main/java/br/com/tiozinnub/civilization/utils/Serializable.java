package br.com.tiozinnub.civilization.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Serializable {
    private final Map<String, SerializableType> types;
    private final Map<String, Supplier<?>> getters;
    private final Map<String, Consumer<?>> setters;
    private final Map<String, Object> defaultValues;
    private final Map<String, Supplier<? extends Serializable>> valueConstructors;

    public Serializable() {
        this.types = new HashMap<>();
        this.getters = new HashMap<>();
        this.setters = new HashMap<>();
        this.defaultValues = new HashMap<>();
        this.valueConstructors = new HashMap<>();
        var helper = new SerializableHelper();
        this.registerProperties(helper);
    }

    public abstract void registerProperties(SerializableHelper helper);

    public NbtCompound toNbt() {
        return toNbt(new NbtCompound());
    }

    @SuppressWarnings("unchecked")
    public NbtCompound toNbt(NbtCompound nbt) {
        for (var entry : this.types.entrySet()) {
            var key = entry.getKey();
            var type = entry.getValue();
            var getter = this.getters.get(key);

            switch (type) {
                case INTEGER -> Optional.ofNullable((Integer) getter.get()).ifPresent(value -> nbt.putInt(key, value));
                case STRING -> Optional.ofNullable((String) getter.get()).ifPresent(value -> nbt.putString(key, value));
                case BLOCK_POS ->
                        Optional.ofNullable((BlockPos) getter.get()).ifPresent(value -> nbt.putLong(key, value.asLong()));
                case MAP_POS ->
                        Optional.ofNullable((MapPos) getter.get()).ifPresent(value -> nbt.putLong(key, value.asLong()));
                case DIRECTION ->
                        Optional.ofNullable((Direction) getter.get()).ifPresent(value -> nbt.putString(key, value.getName()));
                case UUID ->
                        Optional.ofNullable((UUID) getter.get()).ifPresent(value -> nbt.putString(key, value.toString()));
                case BYTE -> Optional.ofNullable((Byte) getter.get()).ifPresent(value -> nbt.putByte(key, value));
                case LIST -> {
                    var list = (List<? extends Serializable>) getter.get();
                    var listNbt = new NbtList();
                    list.stream().map(Serializable::toNbt).forEach(listNbt::add);
                    nbt.put(key, listNbt);
                }
            }
        }

        return nbt;
    }

    public NbtCompound toNbt(NbtCompound nbt, String key) {
        var innerNbt = toNbt();
        nbt.put(key, innerNbt);
        return nbt;
    }

    public Serializable fromNbt(NbtCompound nbt, String key) {
        return fromNbt(nbt.getCompound(key));
    }

    @SuppressWarnings("unchecked")
    public Serializable fromNbt(NbtCompound nbt) {
        if (nbt != null) {
            for (var entry : this.types.entrySet()) {
                var key = entry.getKey();
                var type = entry.getValue();
                var setter = this.setters.get(key);

                var containsKey = nbt.contains(key);

                switch (type) {
                    case INTEGER ->
                            ((Consumer<Integer>) setter).accept(containsKey ? nbt.getInt(key) : (Integer) this.defaultValues.get(key));
                    case STRING ->
                            ((Consumer<String>) setter).accept(containsKey ? nbt.getString(key) : (String) this.defaultValues.get(key));
                    case BLOCK_POS ->
                            ((Consumer<BlockPos>) setter).accept(containsKey ? BlockPos.fromLong(nbt.getLong(key)) : (BlockPos) this.defaultValues.get(key));
                    case MAP_POS ->
                            ((Consumer<MapPos>) setter).accept(containsKey ? MapPos.fromLong(nbt.getLong(key)) : (MapPos) this.defaultValues.get(key));
                    case DIRECTION ->
                            ((Consumer<Direction>) setter).accept(containsKey ? Direction.byName(nbt.getString(key)) : (Direction) this.defaultValues.get(key));
                    case UUID ->
                            ((Consumer<UUID>) setter).accept(containsKey ? UUID.fromString(nbt.getString(key)) : (UUID) this.defaultValues.get(key));
                    case BYTE ->
                            ((Consumer<Byte>) setter).accept(containsKey ? nbt.getByte(key) : (Byte) this.defaultValues.get(key));
                    case LIST -> {
                        var getter = this.getters.get(key);
                        var list = (List<Serializable>) getter.get();
                        nbt.getList(key, 10).stream().map(NbtCompound.class::cast).forEach(n -> list.add(this.valueConstructors.get(key).get().fromNbt(n)));
                    }
                }
            }
        }

        this.afterDeserialized();

        return this;
    }

    public void afterDeserialized() {
        // NO-OP
    }

    protected class SerializableHelper {
        public SerializableHelper() {
        }

        public void registerProperty(String key, Supplier<BlockPos> getter, Consumer<BlockPos> setter, BlockPos defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.BLOCK_POS);
        }

        public void registerProperty(String key, Supplier<MapPos> getter, Consumer<MapPos> setter, MapPos defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.MAP_POS);
        }

        public void registerProperty(String key, Supplier<Integer> getter, Consumer<Integer> setter, Integer defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.INTEGER);
        }

        public void registerProperty(String key, Supplier<String> getter, Consumer<String> setter, String defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.STRING);
        }

        public void registerProperty(String key, Supplier<Direction> getter, Consumer<Direction> setter, Direction defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.DIRECTION);
        }

        public void registerProperty(String key, Supplier<UUID> getter, Consumer<UUID> setter, UUID defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.UUID);
        }

        public void registerProperty(String key, Supplier<Byte> getter, Consumer<Byte> setter, Byte defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.BYTE);
        }

        public <V extends Serializable> void registerProperty(String key, Supplier<List<V>> getter, Supplier<V> valueConstructor) {
            this.registerProperty(key, getter, null, new ArrayList<V>(), SerializableType.LIST);
            valueConstructors.put(key, valueConstructor);
        }

        private void registerProperty(String key, Supplier<?> getter, Consumer<?> setter, Object defaultValue, SerializableType type) {
            types.put(key, type);
            getters.put(key, getter);
            setters.put(key, setter);
            defaultValues.put(key, defaultValue);
        }
    }
}
