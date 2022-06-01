package br.com.tiozinnub.civilization.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Serializable {
    private final Map<String, SerializableType> types;
    private final Map<String, Supplier<?>> getters;
    private final Map<String, Consumer<?>> setters;
    private final Map<String, Object> defaultValues;

    public Serializable() {
        this.types = new HashMap<>();
        this.getters = new HashMap<>();
        this.setters = new HashMap<>();
        this.defaultValues = new HashMap<>();
        var helper = new SerializableHelper();
        this.registerProperties(helper);
    }

    public abstract void registerProperties(SerializableHelper helper);

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        for (var entry : this.types.entrySet()) {
            var key = entry.getKey();
            var type = entry.getValue();
            var getter = this.getters.get(key);

            switch (type) {
                case INTEGER -> Optional.ofNullable((Integer) getter.get()).ifPresent(value -> nbt.putInt(key, value));
                case STRING -> Optional.ofNullable((String) getter.get()).ifPresent(value -> nbt.putString(key, value));
                case BLOCK_POS ->
                        Optional.ofNullable((BlockPos) getter.get()).ifPresent(value -> nbt.putLong(key, value.asLong()));
                case DIRECTION ->
                        Optional.ofNullable((Direction) getter.get()).ifPresent(value -> nbt.putString(key, value.getName()));
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
    protected Serializable fromNbt(NbtCompound nbt) {
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
                    case DIRECTION ->
                            ((Consumer<Direction>) setter).accept(containsKey ? Direction.byName(nbt.getString(key)) : (Direction) this.defaultValues.get(key));
                }
            }
        }

        return this;
    }

    protected class SerializableHelper {
        public SerializableHelper() {
        }

        public void registerProperty(String key, Supplier<BlockPos> getter, Consumer<BlockPos> setter, BlockPos defaultValue) {
            this.registerProperty(key, getter, setter, defaultValue, SerializableType.BLOCK_POS);
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


        private void registerProperty(String key, Supplier<?> getter, Consumer<?> setter, Object defaultValue, SerializableType type) {
            types.put(key, type);
            getters.put(key, getter);
            setters.put(key, setter);
            defaultValues.put(key, defaultValue);
        }
    }
}
