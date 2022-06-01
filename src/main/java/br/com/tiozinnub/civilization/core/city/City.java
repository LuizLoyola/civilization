package br.com.tiozinnub.civilization.core.city;

import br.com.tiozinnub.civilization.core.city.layout.Node;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class City extends Serializable {
    private final ServerWorld world;
    private UUID id;
    public final List<Node> nodes;

    private City(ServerWorld world) {
        this.world = world;
        this.nodes = new ArrayList<>();
    }

    public City(UUID id, ServerWorld world) {
        this(world);
        this.id = id;
    }

    public City(ServerWorld world, NbtCompound nbtCompound) {
        this(world);
        this.fromNbt(nbtCompound);
    }

    public boolean shouldDelete() {
        // TODO
        return false;
    }

    public void tick() {
    }

    public UUID getCityId() {
        return this.id;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("id", () -> this.id, (value) -> this.id = value, UUID.randomUUID());
        helper.registerProperty("nodes", () -> this.nodes, (value) -> this.nodes.add((Node) value), Node::new);
    }
}
