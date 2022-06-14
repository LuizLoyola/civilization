package br.com.tiozinnub.civilization.core;

import br.com.tiozinnub.civilization.core.blueprinting.Blueprint;
import br.com.tiozinnub.civilization.core.math.Area2d;
import br.com.tiozinnub.civilization.core.math.Pos2d;
import br.com.tiozinnub.civilization.core.structure.StructureType;
import br.com.tiozinnub.civilization.registry.BlueprintRegistry;
import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.Serializable;
import br.com.tiozinnub.civilization.utils.helper.RandomHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.firstBlockDown;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.getAllPositions;
import static br.com.tiozinnub.civilization.utils.helper.RandomHelper.pickOne;

public class City extends Serializable {
    private final ServerWorld world;

    private CityMap map;
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
        this.map = new CityMap();
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
        var chunks = this.map.getChunks();
        var centerChunk = new ChunkPos(this.position);
        if (chunks.contains(centerChunk)) chunks.add(centerChunk);
        return chunks;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("id", this::getCityId, (value) -> this.id = value, UUID.randomUUID());
        helper.registerProperty("name", this::getName, (value) -> this.name = value, "");
        helper.registerProperty("position", this::getPosition, (value) -> this.position = value, new BlockPos(0, 0, 0));
        helper.registerProperty("map", () -> this.map, (value) -> this.map = value, CityMap::new);
    }

    public String getName() {
        return name;
    }

    public void markForDeletion() {
        this.markedForDeletion = true;
    }

    public boolean isPosWithinCity(BlockPos pos) {
        return this.map.isEmpty(Pos2d.from(pos));
    }

    public boolean isPosWithinCityChunks(BlockPos pos) {
        return this.getChunks().contains(new ChunkPos(pos));
    }

    public void addStructure(StructureType structureType) {
        var ids = BlueprintRegistry.getBlueprintIdsForStructure(structureType);
        // TODO: pick the correct one

        var blueprintId = ids.get(0);
        var blueprint = BlueprintRegistry.getBlueprint(blueprintId);

        // get possible place
        var width = blueprint.getLengthX();
        var height = blueprint.getLengthZ();
        var maxSteepness = Math.min(width, height) / 4; // arbitrary

        var minDistance = 2;
        var maxDistance = 15;

        var rnd = getWorld().getRandom();
        for (var inflated = minDistance; inflated <= maxDistance; inflated++) {
            var hasStructure = map.hasAny(CityMapTile.STRUCTURE);
            if (hasStructure)
                map.inflate(CityMapTile.STRUCTURE, inflated, true, CityMapTile.STRUCTURE_FINDER);
            else
                map.inflate(Pos2d.from(getPosition()), inflated, true, CityMapTile.STRUCTURE_FINDER);

            var rectangles = map.findRectangles(width, height, CityMapTile.STRUCTURE_FINDER);
            while (!rectangles.isEmpty()) {
                var rectangle = rectangles.remove(rnd.nextInt(rectangles.size()));

                var yValues = rectangle.getAllPositions().stream().map(pos -> firstBlockDown(getWorld(), pos).getY()).toList();

                //noinspection OptionalGetWithoutIsPresent
                var minY = yValues.stream().min(Integer::compare).get();
                var maxY = yValues.stream().max(Integer::compare).get();

                var steepness = Math.abs(maxY - minY);
                if (steepness > maxSteepness) {
                    continue;
                }

                //noinspection OptionalGetWithoutIsPresent
                var averageY = (int) Math.round(yValues.stream().mapToInt(Integer::intValue).average().getAsDouble());

                var box = rectangle.getBox(averageY, averageY + blueprint.getLengthY());

                var possibleDirections = EnumSet.noneOf(CardinalDirection.class);

                var blueprintDir = blueprint.getDirection();

                if (rectangle.getWidth() == width) {
                    // same direction (or square), so add the correct direction and the opposite
                    possibleDirections.add(blueprintDir);
                    possibleDirections.add(blueprintDir.opposite());
                }

                if (rectangle.getWidth() == height) {
                    // width == height (or square), so add the 2 turned directions
                    possibleDirections.add(blueprintDir.right());
                    possibleDirections.add(blueprintDir.left());
                }

                // get the center of the rectangle
                var center = rectangle.getCenter();

                // TODO: the direction should be based on the pathfinding algorithm later
                var directions = center.getDirectionsTo(Pos2d.from(getPosition()));

                var weigthIfOptimalDirection = 3;
                var weightIfNotOptimalDirection = 1;

                var weights = CardinalDirection.ALL.stream().map(d -> new RandomHelper.Weighted<>(d, directions.contains(d) ? weigthIfOptimalDirection : weightIfNotOptimalDirection)).toList();

                var direction = pickOne(rnd, weights);

                // looks ok, commit to it
                this.map.remove(CityMapTile.STRUCTURE_FINDER);
                this.map.set(rectangle, CityMapTile.STRUCTURE);

                // TODO: add the structure to the map
//                var structure = new Structure(this, box, rotatedBlueprint, direction);

                this.buildBlueprintAt(blueprint, box, direction);
                return;
            }
        }

    }

    private void buildBlueprintAt(Blueprint blueprint, Box box, CardinalDirection direction) {
        var b = blueprint.rotate(direction);
        if (b.getLengthX() != box.getXLength() || b.getLengthZ() != box.getZLength()) {
            throw new IllegalStateException("Blueprint size does not match place size");
        }

        box = box.withMaxY(box.minY + b.getLengthY() - 1);
        var offset = new BlockPos(box.minX, box.minY, box.minZ);

        for (BlockPos pos : getAllPositions(box)) {
            try {
                var blockState = b.getBlockState(pos.subtract(offset));
                if (!blockState.isAir()) {
                    this.getWorld().setBlockState(pos, blockState);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum CityMapTile {
        STRUCTURE_FINDER, STRUCTURE

    }

    private static class CityMap extends Area2d<CityMapTile> {
        protected CityMap() {
            super(Map.ofEntries(new AbstractMap.SimpleEntry<>('s', CityMapTile.STRUCTURE), new AbstractMap.SimpleEntry<>('f', CityMapTile.STRUCTURE_FINDER)), CityMapTile.class);
        }
    }
}
