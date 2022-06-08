package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.CardinalDirection;
import br.com.tiozinnub.civilization.utils.Constraints;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public static Blueprint readFromFile(String fileName) {
        return null;
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
            new BlueprintWriter().write(blueprint, fileName, false, false);
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

    private static class BlueprintWriter {
        private final Map<Character, String> blockMap = new HashMap<>();
        private final String blockStateChars;

        private BlueprintWriter() {
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
            sb.append(getBlockStateId(state));

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
                sb.append(COMMENT).append("\n");
                sb.append(COMMENT).append(" Blueprint file for Civilization mod by TiozinNub.").append("\n");
                sb.append(COMMENT).append(" Generated using the blueprint item from within the game at ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(".\n");
                sb.append(COMMENT).append("\n");
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
}
