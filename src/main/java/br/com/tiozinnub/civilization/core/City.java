package br.com.tiozinnub.civilization.core;

import br.com.tiozinnub.civilization.core.math.MapArea;
import br.com.tiozinnub.civilization.core.math.Rectangle;
import br.com.tiozinnub.civilization.core.structure.StructureType;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.*;

public class City extends Serializable {
    private final ServerWorld world;
    private LayoutManager layoutManager;
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
        this.layoutManager = new LayoutManager();
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

    public List<ChunkPos> getChunks() {
        return this.layoutManager.getChunks();
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("id", this::getCityId, (value) -> this.id = value, UUID.randomUUID());
        helper.registerProperty("name", this::getName, (value) -> this.name = value, "");
        helper.registerProperty("position", this::getPosition, (value) -> this.position = value, new BlockPos(0, 0, 0));
        helper.registerProperty("layoutManager", () -> this.layoutManager, (value) -> this.layoutManager = value, LayoutManager::new);
    }

    public String getName() {
        return name;
    }

    public void markForDeletion() {
        this.markedForDeletion = true;
    }

    public boolean isPosWithinCity(BlockPos pos) {
        return this.layoutManager.isPosWithinCity(pos);
    }

    public void addStructure(StructureType structureType) {
        // get possible place
        var place = this.layoutManager.findPlaceForStructure(10, 8, 5, 20);
        if (place == null) throw new IllegalStateException("No place found for structure %s".formatted(structureType.asString()));

        this.layoutManager.placeStructure(place);
    }

    private class LayoutManager extends Serializable {
        private MapArea reservedArea;

        public LayoutManager() {
            this.reservedArea = new MapArea(position.getX(), position.getZ(), 0, 0);
        }

        public BlockPos getCityCenterPosition() {
            return getPosition();
        }

        @Override
        public void registerProperties(SerializableHelper helper) {
            helper.registerProperty("reservedArea", () -> this.reservedArea, (value) -> this.reservedArea = value, MapArea::new);
        }

        public Rectangle findPlaceForStructure(int width, int height, int minDistance, int maxDistance) {
            var possibleArea = this.reservedArea.inflate(maxDistance).subtract(this.reservedArea.inflate(minDistance));
            var possibleSpots = possibleArea.fitRectangle(width, height, true);
            if (possibleSpots.size() == 0) {
                return null;
            }
            return possibleSpots.get(world.getRandom().nextInt(possibleSpots.size()));
        }

        public void placeStructure(Rectangle rectangle) {
            var box = this.settleRectangle(rectangle);
            box = box.withMaxY(box.minY + 5);
            for (var pos : getAllPositions(box)) {
                world.setBlockState(pos, Blocks.STONE.getDefaultState());
            }

            this.reservedArea = this.reservedArea.add(rectangle);
        }

        public Box settleRectangle(Rectangle rectangle) {
            List<Integer> yValues = new ArrayList<>();

            for (var x = rectangle.getLeft(); x < rectangle.getRight(); x++) {
                for (var z = rectangle.getTop(); z < rectangle.getBottom(); z++) {
                    var y = firstBlockDown(world, x, world.getTopY(), z).getY();
                    yValues.add(y);
                }
            }

            if (yValues.size() == 0) {
                return null;
            }

            var minY = yValues.stream().min(Integer::compareTo).get();
            var maxY = yValues.stream().max(Integer::compareTo).get();

            var smallestSize = Math.min(rectangle.getWidth(), rectangle.getHeight());

            if (maxY - minY > smallestSize / 3) {
                throw new IllegalStateException("Area too steep");
            }

            var averageY = yValues.stream().mapToInt(Integer::intValue).average().orElse(0);

            if (averageY == 0) return null;

            ++averageY; // over the ground

            return new Box(new BlockPos(rectangle.getLeft(), averageY, rectangle.getTop()), new BlockPos(rectangle.getRight(), averageY, rectangle.getBottom()));
        }

        public List<ChunkPos> getChunks() {
            var chunkArea = this.reservedArea.add(new Rectangle(this.getCityCenterPosition())).getChunkArea();
            var chunks = new ArrayList<ChunkPos>();
            for (int x = chunkArea.getLeft(); x < chunkArea.getRight(); x++) {
                for (int z = chunkArea.getTop(); z < chunkArea.getBottom(); z++) {
                    if (chunkArea.contains(x, z)) {
                        chunks.add(new ChunkPos(x, z));
                    }
                }
            }
            return chunks;
        }

        public boolean isPosWithinCity(BlockPos pos) {
            return isPosWithin(pos, getChunks());
        }
    }
}
