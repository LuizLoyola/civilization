package br.com.tiozinnub.civilization.core;

import br.com.tiozinnub.civilization.core.blueprinting.Blueprint;
import br.com.tiozinnub.civilization.core.layout.*;
import br.com.tiozinnub.civilization.core.structure.Structure;
import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.MapPos;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.tiozinnub.civilization.utils.helper.ParticleHelper.drawParticleBox;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.firstBlockDown;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.isPosWithin;

public class City extends Serializable {
    public final List<Node> nodes;
    public final List<Street> streets;
    public final List<CityBlock> cityBlocks;
    public final List<Structure> structures;
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
        this.structures = new ArrayList<>();
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

//        this.createCityBlock(center.east(), center.south());
//        this.createCityBlock(center.west().west(), center.south());
//        this.createCityBlock(center.north().north().east(), center.west());
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

    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
    public void tick() {
        var draw = true;
        var drawCenter = draw && true;
        var drawNodes = draw && true;
        var drawStreets = draw && false;
        var drawCityBlocks = draw && true;
        var drawLots = draw && false;
        var drawLotGroups = draw && true;

        if (drawCenter)
            drawParticleBox(this.world, this.positionOffset, ParticleTypes.FLAME);

        for (CityLayoutPart part : this.getLayoutParts()) {
            var particle = switch (part.getClass().getSimpleName()) {
                case "Node" -> drawNodes ? ParticleTypes.SOUL_FIRE_FLAME : null;
                case "Street" -> drawStreets ? ParticleTypes.FLAME : null;
                case "CityBlock" -> drawCityBlocks ? ParticleTypes.SMOKE : null;
                default -> null;
            };

            if (particle != null) {
                drawParticleBox(this.world, part.getBox(), particle);
            }
        }

        if (drawLots) {
            for (Lot lot : this.getAllAvailableLots()) {
                drawParticleBox(this.world, lot.getBox(), ParticleTypes.CRIT);
            }
        }

        if (drawLotGroups) {
            var lotGroups = this.getLotGroupsForBlueprint(new Blueprint(3, 2));

            if (lotGroups.size() > 0) {
                var index = Math.toIntExact(this.getWorld().getTime() / 10 % lotGroups.size());
                var lotGroup = lotGroups.get(index);
                drawParticleBox(this.world, lotGroup.getBox(), ParticleTypes.HAPPY_VILLAGER, lotGroup.getDirection().asDirection(), ParticleTypes.HEART);
            }
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

    public List<Lot> getAvailableLots(CityBlock cityBlock) {
        var maxX = cityBlock.getMaxLotWidth() - 1;
        var maxZ = cityBlock.getMaxLotHeight() - 1;

        var lots = new ArrayList<Lot>();
        for (int x = 0; x <= maxX; x++) {
            for (int z = 0; z <= maxZ; z++) {
                var lot = new Lot(cityBlock, x, z);
                if (this.structures.stream().noneMatch(s -> s.getBox().intersects(lot.getBox()))) {
                    lots.add(lot);
                }
            }
        }

        return lots;
    }

    public List<Lot> getAllAvailableLots() {
        return this.cityBlocks.stream().flatMap(cityBlock -> this.getAvailableLots(cityBlock).stream()).collect(Collectors.toList());
    }

    public List<LotGroup> getLotGroupsForBlueprint(Blueprint blueprint) {
        var allLots = this.getAllAvailableLots();

        // check for lots touching streets
        var lotsAtStreet = allLots.stream().filter(Lot::isFacingStreet).toList();

        List<LotGroup> lotGroups = new ArrayList<>();

        // for each lot
        for (Lot lot : lotsAtStreet) {
            // for each direction it touches street
            for (var dir : lot.getStreetDirections()) {
                List<Lot> lotGroup = new ArrayList<>();
                lotGroup.add(lot);
                var lotWideEnough = true;

                // get required lot amount
                for (int x = 1; x < blueprint.getWidth(); x++) {
                    var pos = dir.right().move(lot.getPos(), x);

                    // check if next lot to the right exists
                    var nextLot = lotsAtStreet.stream()
                            .filter(l -> l.getCityBlock() == lot.getCityBlock())
                            .filter(l -> l.getPos().equals(pos))
                            .findFirst()
                            .orElse(null);

                    if (nextLot == null) {
                        // didnt find
                        lotWideEnough = false;
                        break;
                    }

                    // found
                    lotGroup.add(nextLot);
                }

                // check if found enough
                if (lotWideEnough) {
                    // group add to list with the used direction
                    lotGroups.add(new LotGroup(lotGroup, dir));
                }
            }
        }

        // for each lot group
        for (LotGroup lotGroup : lotGroups) {
            var dir = lotGroup.getDirection();

            List<Lot> lotsToAdd = new ArrayList<>();

            // for each lot in group
            for (var lot : lotGroup.getLots()) {
                // check behind
                for (int z = 1; z < blueprint.getHeight(); z++) {
                    var pos = dir.opposite().move(lot.getPos(), z);

                    var nextLot = allLots.stream()
                            .filter(l -> l.getCityBlock() == lot.getCityBlock())
                            .filter(l -> l.getPos().equals(pos))
                            .findFirst()
                            .orElse(null);

                    if (nextLot == null) {
                        break;
                    }

                    // found
                    lotsToAdd.add(nextLot);
                }
            }

            lotGroup.getLots().addAll(lotsToAdd);
        }

        // remove groups that don't have enough lots
        var requiredAmount = blueprint.getWidth() * blueprint.getHeight();
        lotGroups.removeIf(entry -> entry.getLots().size() < requiredAmount);

        return lotGroups;
    }

    public static class LotGroup {
        private final List<Lot> lots;
        private final CardinalDirection direction;

        public LotGroup(List<Lot> lots, CardinalDirection direction) {
            this.lots = lots;
            this.direction = direction;
        }

        public List<Lot> getLots() {
            return lots;
        }

        public CardinalDirection getDirection() {
            return direction;
        }

        public Box getBox() {
            var minX = this.lots.stream().mapToDouble(l -> l.getBox().minX).min().orElse(0);
            var minY = this.lots.stream().mapToDouble(l -> l.getBox().minY).min().orElse(0);
            var minZ = this.lots.stream().mapToDouble(l -> l.getBox().minZ).min().orElse(0);
            var maxX = this.lots.stream().mapToDouble(l -> l.getBox().maxX).max().orElse(0);
            var maxY = this.lots.stream().mapToDouble(l -> l.getBox().maxY).max().orElse(0);
            var maxZ = this.lots.stream().mapToDouble(l -> l.getBox().maxZ).max().orElse(0);

            return new Box(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
