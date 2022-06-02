package br.com.tiozinnub.civilization.core.city;

import br.com.tiozinnub.civilization.core.city.layout.Node;
import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.MapPos;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static br.com.tiozinnub.civilization.utils.helper.ParticleHelper.drawParticleBox;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.isPosWithin;

public class City extends Serializable {
    private final ServerWorld world;
    private BlockPos positionOffset;
    private UUID id;
    public final List<Node> nodes;
    private String name;
    private boolean markedForDeletion;

    private City(ServerWorld world) {
        this.world = world;
        this.nodes = new ArrayList<>();
    }

    public City(UUID id, ServerWorld world, BlockPos pos) {
        this(world);
        this.id = id;

        this.positionOffset = pos;

        var node = new Node(this, MapPos.ZERO);
        this.nodes.add(node);

        this.createNode(node, CardinalDirection.NORTH);
        this.createNode(node, CardinalDirection.SOUTH);
        this.createNode(node, CardinalDirection.EAST);
        this.createNode(node, CardinalDirection.WEST);
    }

    private Node createNode(Node node, CardinalDirection direction) {
        var newNode = new Node(this, node.getPos().move(direction));
        this.nodes.add(newNode);
        return newNode;
    }

    public City(ServerWorld world, NbtCompound nbtCompound) {
        this(world);
        this.fromNbt(nbtCompound);
    }

    public boolean shouldDelete() {
        return this.markedForDeletion;
    }

    public void tick() {
        drawParticleBox(this.world, this.positionOffset, ParticleTypes.FLAME);

        for (Node node : this.nodes) {
            drawParticleBox(this.world, node.getBox(), ParticleTypes.SOUL_FIRE_FLAME);
        }
    }

    public UUID getCityId() {
        return this.id;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public BlockPos getPositionOffset() {
        return this.positionOffset;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("id", this::getCityId, (value) -> this.id = value, UUID.randomUUID());
        helper.registerProperty("nodes", () -> this.nodes, () -> new Node(this));
        helper.registerProperty("positionOffset", this::getPositionOffset, (value) -> this.positionOffset = value, new BlockPos(0, 0, 0));
        helper.registerProperty("name", this::getName, (value) -> this.name = value, "");
    }

    public LayoutDimensions getLayoutDimensions() {
        return new LayoutDimensions(15, 7);
    }

    public String getName() {
        return name;
    }

    public boolean isPosWithinCity(BlockPos pos) {
        return this.getChunks().stream().anyMatch(chunk -> isPosWithin(pos, chunk));
    }

    public List<ChunkPos> getChunks() {
        var chunks = new ArrayList<ChunkPos>();

        for (Node node : this.nodes) {
            chunks.add(node.getChunk());
        }

        // remove duplicates
        var uniqueChunks = new ArrayList<ChunkPos>();
        for (ChunkPos chunk : chunks) {
            if (uniqueChunks.contains(chunk)) continue;
            uniqueChunks.add(chunk);
        }

        return uniqueChunks;
    }

    public void markForDeletion() {
        this.markedForDeletion = true;
    }
}
