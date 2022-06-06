package br.com.tiozinnub.civilization.core;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class City extends Serializable {
    private final ServerWorld world;
    private BlockPos position;
    private UUID id;
    private String name;
    private boolean markedForDeletion;

    private City(ServerWorld world) {
        this.world = world;
    }

    public City(ServerWorld world, BlockPos pos, String name) {
        this(world);
        this.id = UUID.randomUUID();
        this.position = pos;
        this.name = name;
    }

    public City(ServerWorld world, NbtCompound nbtCompound) {
        this(world);
        this.fromNbt(nbtCompound);
    }

    public boolean shouldDelete() {
        return this.markedForDeletion;
    }

    public void tick() {
    }

    public UUID getCityId() {
        return this.id;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("id", this::getCityId, (value) -> this.id = value, UUID.randomUUID());
        helper.registerProperty("name", this::getName, (value) -> this.name = value, "");
    }

    public String getName() {
        return name;
    }

    public void markForDeletion() {
        this.markedForDeletion = true;
    }

    public boolean isPosWithinCity(BlockPos pos) {
        return false;
    }
}
