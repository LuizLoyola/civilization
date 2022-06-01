package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlueprintMaker extends Serializable {
    public final List<Stage> stages = new ArrayList<>();
    public BlockPos startPos;
    public BlockPos endPos;
    public BlockPos mainBlockPos;
    public Direction direction;

    public static BlueprintMaker newFromNbt(NbtCompound nbt) {
        return (BlueprintMaker) new BlueprintMaker().fromNbt(nbt);
    }

    public Box getBox() {
        return new Box(startPos, endPos);
    }

    public String getNextStepTooltip() {
        if (startPos == null) {
            return "Click to set the first position";
        } else if (endPos == null) {
            return "Click to set the second position";
        } else if (direction == null) {
            return "Click to set the direction";
        } else {
            return "Click a block to finish";
        }
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("firstPos", () -> startPos, (value) -> startPos = value, null);
        helper.registerProperty("secondPos", () -> endPos, (value) -> endPos = value, null);
        helper.registerProperty("direction", () -> direction, (value) -> direction = value, null);
        helper.registerProperty("mainBlockPos", () -> mainBlockPos, (value) -> mainBlockPos = value, null);
    }

    public String generateBlueprintText() {
        var sb = new StringBuilder();
        sb.append(this.stages.size()).append('\n');
        var box = this.getBox();
        assert box != null;
        int sizeX = (int) box.getXLength() + 1;
        int sizeY = (int) box.getYLength() + 1;
        int sizeZ = (int) box.getZLength() + 1;

        sb.append(sizeX);
        sb.append("x");
        sb.append(sizeY);
        sb.append("x");
        sb.append(sizeZ);
        sb.append('\n');

        var dict = new HashMap<Character, BlockState>();
        var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        var charIdx = 0;

        for (var stage : this.stages) {
            for (var y = sizeY - 1; y >= 0; y--) {
                for (var z = 0; z < sizeZ; z++) {
                    for (var x = 0; x < sizeX; x++) {
                        var pos = new BlockPos(x, y, z);
                        if (pos.add(this.getMinPos()).equals(this.mainBlockPos)) {
                            sb.append('.');
                            continue;
                        }

                        var blockState = stage.blockStates.get("%d.%d.%d".formatted(x, y, z));

                        if (blockState.isAir()) {
                            sb.append(" ");
                            continue;
                        }

                        Character c = null;

                        // find existing dict key
                        for (var pair : dict.entrySet()) {
                            if (pair.getValue().equals(blockState)) {
                                c = pair.getKey();
                                break;
                            }
                        }

                        if (c == null) c = chars.charAt(charIdx++);

                        dict.put(c, blockState);

                        sb.append(c);
                    }
                    sb.append("\n");
                }
            }
        }

        for (var pair : dict.entrySet()) {
            sb.append(pair.getKey());
            sb.append("=");

            var state = pair.getValue();

            var blockId = Registry.BLOCK.getId(state.getBlock());
            if (!blockId.getNamespace().equals("minecraft")) {
                sb.append(blockId.getNamespace());
                sb.append(":");
            }
            sb.append(blockId.getPath());

            var defaultState = state.getBlock().getDefaultState();

            // TODO: remove direction of fences

            var properties = state.getProperties().stream().filter(p -> !state.get(p).toString().equals(defaultState.get(p).toString())).map(p -> p.getName() + "=" + state.get(p).toString()).collect(Collectors.joining(","));

            if (properties.length() > 0) {
                sb.append("[");
                sb.append(properties);
                sb.append("]");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public BlockPos getMinPos() {
        return new BlockPos(Math.min(this.startPos.getX(), this.endPos.getX()), Math.min(this.startPos.getY(), this.endPos.getY()), Math.min(this.startPos.getZ(), this.endPos.getZ()));
    }

    public static class Stage {
        private final Map<String, BlockState> blockStates = new HashMap<>();
        private final Vec3i size;

        public Stage(World world, Box box, BlockPos relative) {
            this.size = new BlockPos(box.getXLength(), box.getYLength(), box.getZLength()).add(1, 1, 1);

            for (var x = 0; x < size.getX(); x++) {
                for (var y = 0; y < size.getY(); y++) {
                    for (var z = 0; z < size.getZ(); z++) {
                        blockStates.put("%d.%d.%d".formatted(x, y, z), world.getBlockState(relative.add(x, y, z)));
                    }
                }
            }
        }

        public boolean isSame(Stage other) {
            if (!this.size.equals(other.size)) return false;

            var i = 0;

            for (var x = 0; x < size.getX(); x++) {
                for (var y = 0; y < size.getY(); y++) {
                    for (var z = 0; z < size.getZ(); z++) {
                        if (!this.blockStates.get("%d.%d.%d".formatted(x, y, z)).equals(other.blockStates.get("%d.%d.%d".formatted(x, y, z))))
                            return false;
                    }
                }
            }

            return true;
        }
    }
}
