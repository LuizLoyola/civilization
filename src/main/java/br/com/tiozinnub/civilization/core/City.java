package br.com.tiozinnub.civilization.core;

import br.com.tiozinnub.civilization.core.math.Area2d;
import br.com.tiozinnub.civilization.core.math.Pos2d;
import br.com.tiozinnub.civilization.core.structure.StructureType;
import br.com.tiozinnub.civilization.registry.BlueprintRegistry;
import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.Serializable;
import br.com.tiozinnub.civilization.utils.helper.RandomHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;

import static br.com.tiozinnub.civilization.utils.helper.BlueprintHelper.instaBuildBlueprint;
import static br.com.tiozinnub.civilization.utils.helper.ParticleHelper.drawParticleBox;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.firstBlockDown;
import static br.com.tiozinnub.civilization.utils.helper.RandomHelper.flipACoin;
import static br.com.tiozinnub.civilization.utils.helper.RandomHelper.pickOne;

public class City extends Serializable {
    private final ServerWorld world;
    private final Logger logger;
    private CityMap map;
    private BlockPos position;
    private UUID id;
    private String name;
    private boolean markedForDeletion;

    private City(ServerWorld world) {
        this.world = world;
        this.logger = LogUtils.getLogger();
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
//        drawParticleBox(this.world, this.position, ParticleTypes.FLAME);

        var chunks = getChunks();

        if (chunks.isEmpty()) return;

        var minX = Integer.MAX_VALUE;
        var minZ = Integer.MAX_VALUE;
        var maxX = Integer.MIN_VALUE;
        var maxZ = Integer.MIN_VALUE;

        for (var chunk : chunks) {
            minX = Math.min(minX, chunk.getStartX());
            minZ = Math.min(minZ, chunk.getStartZ());
            maxX = Math.max(maxX, chunk.getEndX());
            maxZ = Math.max(maxZ, chunk.getEndZ());
        }

        var box = new Box(minX, 0, minZ, maxX, 255, maxZ);
        drawParticleBox(this.world, box, ParticleTypes.FLAME);
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
        if (chunks.isEmpty()) chunks = List.of(centerChunk);
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
        return !this.map.isEmpty(Pos2d.from(pos));
    }

    public boolean isPosWithinCityChunks(BlockPos pos) {
        return this.getChunks().contains(new ChunkPos(pos));
    }

    public void addStructure(StructureType structureType) {
        logger.info("Adding structure {} to city {}", structureType.asString(), this.getName());
        var ids = BlueprintRegistry.getBlueprintIdsForStructure(structureType);
        // TODO: pick the correct one

        var blueprintId = ids.get(0);
        var blueprint = BlueprintRegistry.getBlueprint(blueprintId);

        // get possible place
        var width = blueprint.getLengthX();
        var height = blueprint.getLengthZ();
        var maxSteepness = Math.min(width, height) / 4; // arbitrary

        var minDistance = 5;
        var maxDistance = 15;

        var rnd = getWorld().getRandom();
        for (var margin = minDistance; margin <= maxDistance; margin++) {
            var w = width;
            var h = height;

            if (h != w && flipACoin(rnd)) {
                w = height;
                h = width;
            }

            var hasStructure = map.hasAny(CityMapTile.STRUCTURE);
            if (hasStructure) {
                map.inflate(CityMapTile.STRUCTURE, margin, true, CityMapTile.STRUCTURE_FINDER_2);
                map.inflate(CityMapTile.STRUCTURE_FINDER_2, width, height, true, CityMapTile.STRUCTURE_FINDER_1);
                map.remove(CityMapTile.STRUCTURE_FINDER_2);
            } else {
                map.inflate(Pos2d.from(getPosition()), width + margin, height + margin, true, CityMapTile.STRUCTURE_FINDER_1);
            }

            var rectangles = map.findRectangles(w, h, CityMapTile.STRUCTURE_FINDER_1);
            this.map.remove(CityMapTile.STRUCTURE_FINDER_1);

            while (!rectangles.isEmpty()) {
                var rectangle = rectangles.remove(rnd.nextInt(rectangles.size()));
                logger.info("Found rectangle {}", rectangle);

                var yValues = rectangle.getAllPositions().stream().map(pos -> firstBlockDown(getWorld(), pos).getY()).toList();

                //noinspection OptionalGetWithoutIsPresent
                var minY = yValues.stream().min(Integer::compare).get();
                var maxY = yValues.stream().max(Integer::compare).get();

                var steepness = Math.abs(maxY - minY);
                if (steepness > maxSteepness) {
                    logger.info("Steepness {} is too high, skipping", steepness);
                    continue;
                }

                //noinspection OptionalGetWithoutIsPresent
                var averageY = (int) Math.floor(yValues.stream().mapToInt(Integer::intValue).average().getAsDouble());
                logger.info("Average Y is {}", averageY);

                averageY++;

                var box = rectangle.getBox(averageY, averageY + blueprint.getLengthY() - 1);

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

                var weights = possibleDirections.stream().map(d -> new RandomHelper.Weighted<>(d, directions.contains(d) ? weigthIfOptimalDirection : weightIfNotOptimalDirection)).toList();

                var direction = pickOne(rnd, weights);

                // looks ok, commit to it
                this.map.set(rectangle, CityMapTile.STRUCTURE);

                // TODO: add the structure to the map
//                var structure = new Structure(this, box, rotatedBlueprint, direction);

                logger.info("Building blueprint at {}, pointing {}", box, direction);
                instaBuildBlueprint(world, blueprint, box, direction);
                return;
            }

            logger.info("No rectangle found, trying again");
        }

    }

    public enum CityMapTile {
        STRUCTURE_FINDER_1, STRUCTURE_FINDER_2, STRUCTURE
    }

    private static class CityMap extends Area2d<CityMapTile> {
        protected CityMap() {
            super(Map.of('#', CityMapTile.STRUCTURE, ',', CityMapTile.STRUCTURE_FINDER_1, '.', CityMapTile.STRUCTURE_FINDER_2), CityMapTile.class);
        }

        @Override
        public String toString() {
            var sb = new StringBuilder();
            var lines = Arrays.stream(serializeMatrix().split("\n")).toList();
            if (lines.size() > 0) {
                var width = lines.get(0).length();
                sb.append("|").append(StringUtils.repeat("-", width)).append("|\n");
                for (var line : lines) {
                    sb.append("|").append(line).append("|\n");
                }
                sb.append("|").append(StringUtils.repeat("-", width)).append("|\n");
            }

            return "Area at %d %d:\n%s".formatted(getOffsetX(), getOffsetZ(), sb.toString());
        }
    }
}
