package br.com.tiozinnub.civilization.core.ai.movement.pathing;

import br.com.tiozinnub.civilization.core.ai.movement.Step;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static br.com.tiozinnub.civilization.utils.helper.StringHelper.getStringFromBlockPos;

public class Path extends Serializable {
    private final List<Step> steps;
    private BlockPos start;
    private String pathData;

    public Path(NbtCompound nbt) {
        this.steps = new ArrayList<>();
        fromNbt(nbt);
    }

    public Path(BlockPos start) {
        this.start = start;
        this.steps = new ArrayList<>();
        this.pathData = null;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("pathData", this::getPathData, this::setPathData, null);
    }

    public String getPathData() {
        if (this.pathData != null) return this.pathData;
        this.pathData = this.serializePath();
        return this.pathData;
    }

    private void setPathData(String pathData) {
        this.pathData = pathData;
        this.deserializePath(pathData);
    }

    private String serializePath() {
        var sb = new StringBuilder();

        // append start position
        sb.append(getStringFromBlockPos(start, false)).append("|");

        var lastPos = start;

        // append steps
        for (var step : steps) {
            // step directions based on last
            var xDiff = step.toPos().getX() - lastPos.getX();
            var yDiff = step.toPos().getY() - lastPos.getY();
            var zDiff = step.toPos().getZ() - lastPos.getZ();

            lastPos = step.toPos();

            char xChar = xDiff > 0 ? 'E' : xDiff < 0 ? 'W' : '-';
            var yChar = yDiff > 0 ? 'U' : yDiff < 0 ? 'D' : '-';
            var zChar = zDiff > 0 ? 'S' : zDiff < 0 ? 'N' : '-';

            xDiff = Math.abs(xDiff);
            yDiff = Math.abs(yDiff);
            zDiff = Math.abs(zDiff);

            if (xDiff > 0) sb.append(String.valueOf(xChar).repeat(xDiff));
            if (yDiff > 0) sb.append(String.valueOf(yChar).repeat(yDiff));
            if (zDiff > 0) sb.append(String.valueOf(zChar).repeat(zDiff));

            sb.append("|");
        }

        return sb.toString();
    }

    private void deserializePath(String data) {
        steps.clear();

        var idx = 0;
        var first = true;
        BlockPos lastPos = null;

        while (idx < data.length()) {
            var sb = new StringBuilder();
            while (true) {
                var c = data.charAt(idx++);
                if (c == '|') break;
                sb.append(c);
            }

            if (first) {
                var pos = sb.toString().split(" ");
                this.start = new BlockPos(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));
                lastPos = start;
                steps.add(new Step(null, start));
                first = false;
            } else {
                var pos = lastPos;

                for (int i = 0; i < sb.length(); i++) {
                    var c = sb.charAt(i);
                    switch (c) {
                        case 'E' -> pos = pos.east();
                        case 'W' -> pos = pos.west();
                        case 'U' -> pos = pos.up();
                        case 'D' -> pos = pos.down();
                        case 'S' -> pos = pos.south();
                        case 'N' -> pos = pos.north();
                    }
                }

                steps.add(new Step(lastPos, pos));
                lastPos = pos;
            }
        }
    }

    public void addStep(Step step) {
        this.steps.add(step);
    }

    public List<Step> getSteps() {
        return steps;
    }

    public BlockPos getStart() {
        return start;
    }
}
