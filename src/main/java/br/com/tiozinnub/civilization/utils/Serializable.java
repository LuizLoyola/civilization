package br.com.tiozinnub.civilization.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static br.com.tiozinnub.civilization.utils.helper.NbtHelper.get;
import static br.com.tiozinnub.civilization.utils.helper.NbtHelper.put;

public abstract class Serializable {
    private final Map<String, SerializableType> types;
    private final Map<String, Supplier<?>> getters;
    private final Map<String, Consumer<?>> setters;
    private final Map<String, Object> defaultValues;
    private final Map<String, Supplier<? extends Serializable>> constructors;

    public Serializable() {
        this.types = new HashMap<>();
        this.getters = new HashMap<>();
        this.setters = new HashMap<>();
        this.defaultValues = new HashMap<>();
        this.constructors = new HashMap<>();
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
                case INTEGER -> Optional.ofNullable((Integer) getter.get()).ifPresent(value -> put(nbt, key, value));
                case STRING -> Optional.ofNullable((String) getter.get()).ifPresent(value -> put(nbt, key, value));
                case BOOLEAN -> Optional.ofNullable((Boolean) getter.get()).ifPresent(value -> put(nbt, key, value));
                case BLOCK_POS -> Optional.ofNullable((BlockPos) getter.get()).ifPresent(value -> put(nbt, key, value));
                case DIRECTION -> Optional.ofNullable((Direction) getter.get()).ifPresent(value -> put(nbt, key, value));
                case CARDINAL_DIRECTION -> Optional.ofNullable((CardinalDirection) getter.get()).ifPresent(value -> put(nbt, key, value));
                case UUID -> Optional.ofNullable((UUID) getter.get()).ifPresent(value -> put(nbt, key, value));
                case BYTE -> Optional.ofNullable((Byte) getter.get()).ifPresent(value -> put(nbt, key, value));
                case BOX -> Optional.ofNullable((Box) getter.get()).ifPresent(value -> put(nbt, key, value));
                case SERIALIZABLE -> Optional.ofNullable((Serializable) getter.get()).ifPresent(value -> put(nbt, key, value));
                case LIST -> {
                    var listNbt = new NbtList();
                    ((List<? extends Serializable>) getter.get()).stream().map(Serializable::toNbt).forEach(listNbt::add);
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

    public Set<Map.Entry<String, SerializableType>> customKeyOrder(Set<Map.Entry<String, SerializableType>> entrySet) {
        // place SERIALIZABLEs last
        var serializableSet = entrySet.stream().filter(entry -> entry.getValue() == SerializableType.SERIALIZABLE).collect(Collectors.toSet());
        var otherSet = entrySet.stream().filter(entry -> entry.getValue() != SerializableType.SERIALIZABLE).collect(Collectors.toSet());
        var result = new LinkedHashSet<>(otherSet);
        result.addAll(serializableSet);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Serializable fromNbt(NbtCompound nbt) {
        if (nbt != null) {
            for (var entry : customKeyOrder(this.types.entrySet())) {
                var key = entry.getKey();
                var type = entry.getValue();
                var setter = this.setters.get(key);

                switch (type) {
                    case INTEGER -> ((Consumer<Integer>) setter).accept(get(nbt, key, (Integer) this.defaultValues.get(key)));
                    case STRING -> ((Consumer<String>) setter).accept(get(nbt, key, (String) this.defaultValues.get(key)));
                    case BOOLEAN -> ((Consumer<Boolean>) setter).accept(get(nbt, key, (Boolean) this.defaultValues.get(key)));
                    case BLOCK_POS -> ((Consumer<BlockPos>) setter).accept(get(nbt, key, (BlockPos) this.defaultValues.get(key)));
                    case DIRECTION -> ((Consumer<Direction>) setter).accept(get(nbt, key, (Direction) this.defaultValues.get(key), Direction::byName));
                    case CARDINAL_DIRECTION ->
                            ((Consumer<CardinalDirection>) setter).accept(get(nbt, key, (CardinalDirection) this.defaultValues.get(key), CardinalDirection::byName));
                    case UUID -> ((Consumer<UUID>) setter).accept(get(nbt, key, (UUID) this.defaultValues.get(key)));
                    case BYTE -> ((Consumer<Byte>) setter).accept(get(nbt, key, (Byte) this.defaultValues.get(key)));
                    case BOX -> ((Consumer<Box>) setter).accept(get(nbt, key, (Box) this.defaultValues.get(key)));
                    case SERIALIZABLE -> ((Consumer<Serializable>) setter).accept(get(nbt, key, this.constructors.get(key)));
                    case LIST -> {
                        var getter = this.getters.get(key);
                        var list = (List<Serializable>) getter.get();
                        nbt.getList(key, 10).stream().map(NbtCompound.class::cast).forEach(n -> list.add(this.constructors.get(key).get().fromNbt(n)));
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

        public void registerProperty(String key, Supplier<Integer> getter, Consumer<Integer> setter, int defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.INTEGER);
        }

        public void registerProperty(String key, Supplier<String> getter, Consumer<String> setter, String defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.STRING);
        }

        public void registerProperty(String key, Supplier<Boolean> getter, Consumer<Boolean> setter, boolean defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.BOOLEAN);
        }

        public void registerProperty(String key, Supplier<Direction> getter, Consumer<Direction> setter, Direction defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.DIRECTION);
        }

        public void registerProperty(String key, Supplier<CardinalDirection> getter, Consumer<CardinalDirection> setter, CardinalDirection defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.CARDINAL_DIRECTION);
        }

        public void registerProperty(String key, Supplier<UUID> getter, Consumer<UUID> setter, UUID defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.UUID);
        }

        public void registerProperty(String key, Supplier<Byte> getter, Consumer<Byte> setter, byte defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.BYTE);
        }

        public void registerProperty(String key, Supplier<Box> getter, Consumer<Box> setter, Box defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.BOX);
        }

        public <V extends Serializable> void registerProperty(String key, Supplier<V> getter, Consumer<V> setter, Supplier<V> constructor) {
            this.registerProperty(key, getter, setter, null, SerializableType.SERIALIZABLE);
            constructors.put(key, constructor);
        }

        public <V extends Serializable> void registerProperty(String key, Supplier<List<V>> getter, Supplier<V> valueConstructor) {
            this.registerProperty(key, getter, null, new ArrayList<V>(), SerializableType.LIST);
            constructors.put(key, valueConstructor);
        }

        private void registerProperty(String key, Supplier<?> getter, Consumer<?> setter, Object defaultValue, SerializableType type) {
            types.put(key, type);
            getters.put(key, getter);
            setters.put(key, setter);
            defaultValues.put(key, defaultValue);
        }
    }
}
