package br.com.tiozinnub.civilization.core;

import br.com.tiozinnub.civilization.core.layout.*;
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
import java.util.stream.Stream;

import static br.com.tiozinnub.civilization.utils.helper.ParticleHelper.drawParticleBox;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.firstBlockDown;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.isPosWithin;

public class City extends Serializable {
    public final List<Node> nodes;
    public final List<Street> streets;
    public final List<CityBlock> cityBlocks;
    private final ServerWorld world;
    private BlockPos positionOffset;
    private UUID id;
    private String name;
    private boolean markedForDeletion;
    private LayoutDimensions layoutDimensions;

    private City(ServerWorld world) {
        this.world = world;
        this.nodes = new ArrayList<>();
        this.streets = new ArrayList<>();
        this.cityBlocks = new ArrayList<>();
    }

    public City(UUID id, ServerWorld world, BlockPos pos) {
        this(id, world, pos, new LayoutDimensions(17, 7));
    }

    public City(UUID id, ServerWorld world, BlockPos pos, LayoutDimensions layoutDimensions) {
        this(world);
        this.id = id;
        this.layoutDimensions = layoutDimensions;
        this.positionOffset = pos;

        var center = MapPos.ZERO;

        var node = new Node(this, center);
        this.nodes.add(node);

        this.createNode(center.south());
        this.createNode(center.east());
        this.createNode(center.west());

        this.createStreet(center, center.south());
        this.createStreet(center, center.east());
        this.createStreet(center, center.west());

        this.createCityBlock(center.east(), center.south());
        this.createCityBlock(center.west(), center.south());
        this.createCityBlock(center.north().east(), center.west());
    }

    public City(ServerWorld world, NbtCompound nbtCompound) {
        this(world);
        this.fromNbt(nbtCompound);
    }

    private Node createNode(MapPos pos) {
        var node = new Node(this, pos);
        this.nodes.add(node);
        return node;
    }

    private Street createStreet(MapPos pos1, MapPos pos2) {
        var street = new Street(this, pos1, pos2);
        this.streets.add(street);
        return street;
    }

    private CityBlock createCityBlock(MapPos pos1, MapPos pos2) {
        var cityBlock = new CityBlock(this, pos1, pos2);
        this.cityBlocks.add(cityBlock);
        return cityBlock;
    }

    public Node getNode(MapPos pos) {
        return this.nodes
                .stream()
                .filter(node -> node.getPos().equals(pos))
                .findFirst()
                .orElse(null);
    }

    public Street getStreet(MapPos pos1, MapPos pos2) {
        return this.streets
                .stream()
                .filter(street -> street.getPos1().equals(pos1) && street.getPos2().equals(pos2))
                .findFirst()
                .orElse(null);
    }

    public boolean shouldDelete() {
        return this.markedForDeletion;
    }

    public void tick() {
        drawParticleBox(this.world, this.positionOffset, ParticleTypes.FLAME);

        for (CityLayoutPart part : this.getLayoutParts()) {
            var particle = part instanceof Node ? ParticleTypes.SOUL_FIRE_FLAME : part instanceof Street ? ParticleTypes.FLAME : ParticleTypes.SMOKE;
            drawParticleBox(this.world, part.getBox(), particle);
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
        helper.registerProperty("name", this::getName, (value) -> this.name = value, "");
        helper.registerProperty("positionOffset", this::getPositionOffset, (value) -> this.positionOffset = value, new BlockPos(0, 0, 0));
        helper.registerProperty("layoutDimensions", this::getLayoutDimensions, (value) -> this.layoutDimensions = value, LayoutDimensions::new);

        helper.registerProperty("nodes", () -> this.nodes, () -> new Node(this));
        helper.registerProperty("streets", () -> this.streets, () -> new Street(this));
        helper.registerProperty("cityBlocks", () -> this.cityBlocks, () -> new CityBlock(this));
    }

    public LayoutDimensions getLayoutDimensions() {
        return this.layoutDimensions;
    }

    public String getName() {
        return name;
    }

    public boolean isPosWithinCity(BlockPos pos) {
        return this.getChunks().stream().anyMatch(chunk -> isPosWithin(pos, chunk));
    }

    public List<ChunkPos> getChunks() {
        var chunks = new ArrayList<ChunkPos>();

        for (CityLayoutPart part : this.getLayoutParts()) {
            chunks.addAll(part.getChunks());
        }

        // remove duplicates
        var uniqueChunks = new ArrayList<ChunkPos>();
        for (ChunkPos chunk : chunks) {
            if (uniqueChunks.contains(chunk)) continue;
            uniqueChunks.add(chunk);
        }

        return uniqueChunks;
    }

    private List<CityLayoutPart> getLayoutParts() {
        var all = new ArrayList<CityLayoutPart>();
        Stream.of(this.nodes, this.streets, this.cityBlocks).forEach(all::addAll);
        return all;
    }

    public void markForDeletion() {
        this.markedForDeletion = true;
    }

    public CityLayoutPart getLayoutPartAt(BlockPos blockPos) {
        return this.getLayoutParts().stream().filter(part -> isPosWithin(blockPos, part.getBox(), true)).findFirst().orElse(null);
    }

    public BlockPos getRealPosFor(MapPos mapPos) {
        var offset = this.getPositionOffset();
        var posX = mapPos.getX() * getLayoutDimensions().getNodeDistance() + offset.getX();
        var posY = offset.getY();
        var posZ = mapPos.getZ() * getLayoutDimensions().getNodeDistance() + offset.getZ();
        posY = firstBlockDown(getWorld(), posX, posY, posZ).getY();
        return new BlockPos(posX, posY, posZ);
    }
}
