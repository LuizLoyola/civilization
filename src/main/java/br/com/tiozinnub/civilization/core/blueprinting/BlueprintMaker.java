package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.Constraints;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.blockPosString;
import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.getAllPositions;

public class BlueprintMaker extends Serializable {
    private static final Character AIR = '.';
    private static final Character COMMENT = '#';
    private static final Character IGNORE = '*';
    private static final Character GROUND = '_';
    private static final Character DIRECTION = '>';
    private final ServerWorld world;
    private BlockPos firstPos;
    private BlockPos secondPos;
    private CardinalDirection direction;

    public BlueprintMaker(ServerWorld world) {
        this.world = world;
    }

    private static String getFileName() {
        // blueprint folder on current user's desktop
        var sb = new StringBuilder();
        sb.append(System.getProperty("user.home"));
        sb.append(File.separator);
        sb.append("Desktop");
        sb.append(File.separator);
        sb.append("blueprints");

        // confirm folder exists
        var dir = new File(sb.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        sb.append(File.separator);
        sb.append(System.currentTimeMillis());
        sb.append(".blueprint");
        return sb.toString();
    }

    public Box getBox() {
        if (this.firstPos == null) return null;
        if (this.secondPos == null) return new Box(firstPos, firstPos);
        return new Box(firstPos, secondPos);
    }

    public CardinalDirection getDirection() {
        return direction;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("firstPos", () -> this.firstPos, (value) -> this.firstPos = value, null);
        helper.registerProperty("secondPos", () -> this.secondPos, (value) -> this.secondPos = value, null);
        helper.registerProperty("direction", () -> this.direction, (value) -> this.direction = value, null);
    }

    public Text usedOnBlock(World world, BlockPos blockPos, Direction side, boolean sneaking) {
        if (this.firstPos == null) {
            this.firstPos = blockPos;
            return Text.of("First position: %s. Now set the second position.".formatted(blockPosString(blockPos)));
        } else if (this.secondPos == null) {
            this.secondPos = blockPos;
            return Text.of("Second position: %s. Now set the direction.".formatted(blockPosString(blockPos)));
        } else if (this.direction == null) {
            this.direction = CardinalDirection.fromDirection(side);
            if (this.direction == null) {
                return Text.of("You clicked the %s side of the block. Click again to set the direction.".formatted(side.getName()));
            }

            return Text.of("Structure is facing %s. Click again to save.".formatted(this.direction.getName()));
        } else {
            var blueprint = new BlueprintGenerator().generateBlueprint();
            var fileName = getFileName();
            new Writer().write(blueprint, fileName, false, false);
            return Text.of("Saved blueprint to file: %s".formatted(fileName));
        }
    }

    public Text usedOnAir(ServerWorld world, boolean sneaking) {
        if (sneaking) {
            this.clear();

            return Text.of("Positions cleared.");
        }

        return null;
    }

    private void clear() {
        this.firstPos = null;
        this.secondPos = null;
        this.direction = null;
    }

    private class BlueprintGenerator {

        public BlueprintGenerator() {
        }

        public Blueprint generateBlueprint() {
            var box = getBox();
            var blueprint = new Blueprint((int) box.getXLength() + 1, (int) box.getYLength() + 1, (int) box.getZLength() + 1);
            blueprint.direction = direction;

            var allPos = getAllPositions(box);
            var min = new BlockPos(box.minX, box.minY, box.minZ);
            for (var pos : allPos) {
                var block = world.getBlockState(pos);
                var offset = pos.subtract(min);
                blueprint.blockStates[offset.getX()][offset.getY()][offset.getZ()] = block;
            }

            return blueprint;
        }
    }

    private static class Writer {
        private final Map<Character, String> blockMap = new HashMap<>();
        private final String blockStateChars;

        private Writer() {
            var sb = new StringBuilder();
            for (var c = 'a'; c <= 'z'; c++) sb.append(c);
            for (var c = 'A'; c <= 'Z'; c++) sb.append(c);
            for (var c = '0'; c <= '9'; c++) sb.append(c);

            this.blockStateChars = sb.toString();
        }

        private Identifier getBlockStateId(BlockState state) {
            return Registry.BLOCK.getId(state.getBlock());
        }

        private String serializeBlockState(BlockState state) {
            var sb = new StringBuilder();
            var id = getBlockStateId(state);
            if (id.getNamespace().equals("minecraft")) {
                sb.append(id.getPath());
            } else {
                sb.append(id);
            }

            var propMap = getPropMap(state);
            var defaultPropMap = getPropMap(state.getBlock().getDefaultState());

            var relevantProps = new HashMap<String, String>();
            for (var entry : propMap.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (defaultPropMap.containsKey(key)) {
                    var defaultValue = defaultPropMap.get(key);
                    if (defaultValue.equals(value)) continue;
                }

                relevantProps.put(key, value);
            }

            if (!relevantProps.isEmpty()) {
                sb.append("[");
                sb.append(relevantProps.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",")));
                sb.append("]");
            }
            return sb.toString();
        }

        private Map<String, String> getPropMap(BlockState state) {
            var str = state.toString();
            var start = str.indexOf("[");
            var end = str.indexOf("]");
            if (start == -1 || end == -1) return Collections.emptyMap();
            var propsMap = new HashMap<String, String>();
            var props = str.substring(start + 1, end).split(",");
            for (var prop : props) {
                var split2 = prop.split("=");
                propsMap.put(split2[0], split2[1]);
            }

            return propsMap;
        }

        private Character getCharForBlockState(BlockState block) {
            if (block == null) return IGNORE;
            if (block.isAir()) return AIR;

            var serialized = serializeBlockState(block);
            for (var entry : blockMap.entrySet()) {
                if (entry.getValue().equals(serialized)) return entry.getKey();
            }

            var c = blockStateChars.charAt(blockMap.size());
            blockMap.put(c, serialized);
            return c;
        }

        public void write(Blueprint blueprint, String fileName, boolean suppressComments, boolean alignNorth) {
            if (alignNorth) {
                write(blueprint.rotate(CardinalDirection.NORTH), fileName, suppressComments, false);
                return;
            }

            var sb = new StringBuilder();
            sb.append(Constraints.MOD_NAME).append("\n");
            sb.append(Constraints.MOD_VERSION).append("\n");
            if (!suppressComments) {
                sb.append(COMMENT).append(" Blueprint file for Civilization mod by TiozinNub.").append("\n");
                sb.append(COMMENT).append(" Generated using the blueprint item from within the game at ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(".\n");
            }

            // write the size
            sb.append('(').append(blueprint.getLengthX()).append(' ').append(blueprint.getLengthY()).append(' ').append(blueprint.getLengthZ()).append(')').append('\n');

            // direction
            sb.append(DIRECTION).append(blueprint.direction.getName()).append("\n");

            // blocks
            for (var y = blueprint.getLengthY() - 1; y >= 0; y--) {
                if (!suppressComments) {
                    sb.append(COMMENT).append(" Layer ").append(y).append(":\n");
                }
                for (int x = 0; x < blueprint.getLengthX(); x++) {
                    for (int z = 0; z < blueprint.getLengthZ(); z++) {
                        var block = blueprint.blockStates[x][y][z];
                        var c = getCharForBlockState(block);
                        sb.append(c);
                    }

                    sb.append('\n');
                }
            }

            // block map
            if (!suppressComments) {
                sb.append(COMMENT).append(" Block mapping:\n");
            }
            for (var entry : blockMap.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            if (!suppressComments) {
                sb.append(COMMENT).append(' ').append(AIR).append("=<air>\n");
                sb.append(COMMENT).append(' ').append(IGNORE).append("=<ignore>\n");
                sb.append(COMMENT).append(' ').append(GROUND).append("=<ground>\n");
            }

            var str = sb.toString();

            try {
                // write to file
                var file = new File(fileName);
                var writer = new FileWriter(file);
                writer.write(str);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Reader {
        public Blueprint read(String fileName, String content) {
            var lines = Arrays.stream(content.split("\n")).map(String::trim).filter(l -> !l.startsWith(String.valueOf(COMMENT))).toList();
            var modName = lines.get(0);
            var modVersion = lines.get(1);

            if (!modName.equals(Constraints.MOD_NAME)) {
                throw new IllegalArgumentException("Invalid mod name: " + modName);
            }

            if (!modVersion.equals(Constraints.MOD_VERSION)) {
                // TODO: should have backwards compatibility
                throw new IllegalArgumentException("Invalid mod version: " + modVersion);
            }

            var sizeSplit = lines.get(2).split(" ");
            var lengthX = Integer.parseInt(sizeSplit[0]);
            var lengthY = Integer.parseInt(sizeSplit[1]);
            var lengthZ = Integer.parseInt(sizeSplit[2]);
            var blueprint = new Blueprint(lengthX, lengthY, lengthZ);
            blueprint.direction = CardinalDirection.byName(lines.get(3));

            // start reading blocks
            var blocks = new char[lengthX][lengthY][lengthZ];
            var currLine = 4;
            for (int y = lengthY - 1; y >= 0; y--) {
                for (int x = 0; x < lengthX; x++) {
                    var line = lines.get(currLine++);
                    for (int z = 0; z < lengthZ; z++) {
                        blocks[x][y][z] = line.charAt(z);
                    }
                }
            }

            // should be at the start of the block map
            var blockMap = new HashMap<Character, BlockState>();

            while (currLine < lines.size()) {
                var line = lines.get(currLine++);
                var c = line.charAt(0);
                var blockStateStr = line.substring(2);
                var identifierStr = blockStateStr.replaceAll("\\[.*", "");
                var identifier = new Identifier(identifierStr);
                var blockState = Registry.BLOCK.get(identifier).getDefaultState();

                if (blockStateStr.contains("[")) {
                    var propsStr = blockStateStr.substring(blockStateStr.indexOf("[") + 1, blockStateStr.indexOf("]"));
                    var props = Arrays.stream(propsStr.split(",")).toList();
                    for (var prop : props) {
                        var split = prop.split("=");
                        var propName = split[0];
                        var propValue = split[1];

                        var property = blockState.getBlock().getStateManager().getProperty(propName);
                        if (property == null) {
                            throw new IllegalArgumentException("Invalid property: " + propName);
                        }
                        blockState = blockStateWithProperty(blockState, property, propValue);
                    }
                }

                blockMap.put(c, blockState);
            }

            // set the blocks
            for (int x = 0; x < lengthX; x++) {
                for (int y = 0; y < lengthY; y++) {
                    for (int z = 0; z < lengthZ; z++) {
                        var c = blocks[x][y][z];

                        BlockState blockState;

                        if (c == AIR) {
                            blockState = Blocks.AIR.getDefaultState();
                        } else if (c == IGNORE) {
                            blockState = null;
                        } else if (c == GROUND) {
                            // TODO: treat differently
                            blockState = null;
                        } else {
                            blockState = blockMap.get(c);
                        }

                        if (blockState == null) {
                            throw new IllegalArgumentException("Invalid block state: " + c);
                        }
                        blueprint.blockStates[x][y][z] = blockState;
                    }
                }
            }

            return blueprint;
        }

        private <T extends Comparable<T>> BlockState blockStateWithProperty(BlockState blockState, Property<T> property, String value) {
            var optional = property.parse(value);
            if (optional.isEmpty()) {
                throw new IllegalArgumentException("Invalid value for property: " + property.getName() + "=" + value);
            }

            return blockState.with(property, optional.get());
        }
    }
}
