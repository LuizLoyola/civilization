package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.resource.blueprinting.RawBlueprint;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class Blueprint {
    private final Identifier identifier;
    private final Map<Character, BlockState> blockDictionary = new HashMap<>();
    private final List<StageData> stages = new ArrayList<>();
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final BlockPos mainBlockPos;

    public Blueprint(RawBlueprint rawBlueprint) {
        this.identifier = rawBlueprint.id();

        var lines = Arrays
                .stream(rawBlueprint
                        .text()
                        .split("\n")
                )
                .map(String::stripTrailing)
                .toList();

        var currLine = 0;
        var line = lines.get(currLine++);

        var stageCount = Integer.parseInt(line);

        BlockPos mainBlockPos = null;

        for (var stageIndex = 0; stageIndex < stageCount; stageIndex++) {
            line = lines.get(currLine++);

            var stageDimensionsList = line.split("x");
            var stageX = Integer.parseInt(stageDimensionsList[0]);
            var stageY = Integer.parseInt(stageDimensionsList[1]);
            var stageZ = Integer.parseInt(stageDimensionsList[2]);

            var stageLines = new ArrayList<String>();

            for (int y = stageY - 1; y >= 0; y--) {
                for (int z = 0; z < stageZ; z++) {
                    line = lines.get(currLine++);

                    if (mainBlockPos == null) {
                        var dotPos = line.indexOf('.');
                        if (dotPos != -1) {
                            mainBlockPos = new BlockPos(dotPos, y, z);
                        }
                    }

                    stageLines.add(line);
                }
            }

            stages.add(new StageData(stageLines, stageX, stageY, stageZ));
        }

        this.mainBlockPos = mainBlockPos != null ? mainBlockPos : BlockPos.ORIGIN;

        blockDictionary.put(' ', Blocks.AIR.getDefaultState());
        blockDictionary.put('*', null);
        blockDictionary.put('.', null);

        // no more stages, only block definitions now
        while (currLine < lines.size()) {
            line = lines.get(currLine++);

            var c = line.charAt(0);
            var blockDataStr = line.substring(2);
            var sr = new StringReader(blockDataStr);
            var parser = new BlockArgumentParser(sr, false);

            var blockState = Blocks.AIR.getDefaultState();

            try {
                blockState = parser.parse(false).getBlockState();
            } catch (CommandSyntaxException e) {
                // failed
                //continue;
            }

            blockDictionary.put(c, blockState);
        }

        this.sizeX = this.stages.stream().map(s -> s.sizeX).max(Integer::compare).orElse(0);
        this.sizeY = this.stages.stream().map(s -> s.sizeY).max(Integer::compare).orElse(0);
        this.sizeZ = this.stages.stream().map(s -> s.sizeZ).max(Integer::compare).orElse(0);
    }

    public BlockPos getMainBlockPos() {
        return this.mainBlockPos;
    }

    private char getBlockCharAt(int stageIndex, int x, int y, int z) {
        if (stageIndex >= stages.size()) return ' ';
        var stage = stages.get(stageIndex);
        return stage.get(x, y, z);
    }

    public BlockState getBlockAt(int stageIndex, BlockPos pos) {
        return getBlockAt(stageIndex, pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockAt(int stageIndex, int x, int y, int z) {
        var c = getBlockCharAt(stageIndex, x, y, z);
        return blockDictionary.get(c);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public boolean isBlockIgnored(BlockPos b) {
        return getBlockAt(0, b) == null;
    }

    private record StageData(List<String> stageLines, int sizeX, int sizeY, int sizeZ) {
        public char get(int x, int y, int z) {
            if (x >= sizeX || y >= sizeY || z >= sizeZ) return ' ';

            var flippedY = sizeY - y - 1;

            var line = stageLines.get(flippedY * sizeZ + z);
            if (x >= line.length()) return ' ';
            return line.charAt(x);
        }
    }
}