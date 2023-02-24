package br.com.tiozinnub.civilization.entity.base;

import br.com.tiozinnub.civilization.utils.Serializable;
import br.com.tiozinnub.civilization.utils.helper.PositionHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static br.com.tiozinnub.civilization.utils.helper.PositionHelper.up;
import static br.com.tiozinnub.civilization.utils.helper.StringHelper.getStringFromVec3d;

public class Path extends Serializable {
    private final List<Step> steps;
    private Vec3d start;
    private String pathData;

    public Path(NbtCompound nbt) {
        this.steps = new ArrayList<>();
        fromNbt(nbt);
    }

    public Path(Vec3d start) {
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
        sb.append(getStringFromVec3d(start, false)).append("|");

        var lastPos = start;

        // append steps
        for (var step : steps) {
            // step directions based on last
            var xDiff = (int) (step.toPos().getX() - lastPos.getX());
            var yDiff = step.toPos().getY() - lastPos.getY();
            var zDiff = (int) (step.toPos().getZ() - lastPos.getZ());

            lastPos = step.toPos();

            var xChar = xDiff > 0 ? 'E' : xDiff < 0 ? 'W' : '-';
            var yChar = yDiff > 0 ? 'U' : yDiff < 0 ? 'D' : '-';
            var zChar = zDiff > 0 ? 'S' : zDiff < 0 ? 'N' : '-';

            xDiff = Math.abs(xDiff);
            zDiff = Math.abs(zDiff);

            if (xDiff > 0) sb.append(String.valueOf(xChar).repeat(xDiff));
            if (yDiff != 0) {
                sb.append(yChar).append("(").append(yDiff).append(")");
            }
            if (zDiff > 0) sb.append(String.valueOf(zChar).repeat(zDiff));

            sb.append("|");
        }

        return sb.toString();
    }

    private void deserializePath(String data) {
        steps.clear();

        var idx = 0;
        var first = true;
        Vec3d lastPos = null;

        while (idx < data.length()) {
            var sb = new StringBuilder();
            while (true) {
                var c = data.charAt(idx++);
                if (c == '|') break;
                sb.append(c);
            }

            if (first) {
                var pos = sb.toString().split(" ");
                this.start = new Vec3d(Integer.parseInt(pos[0]), Double.parseDouble(pos[1]), Integer.parseInt(pos[2]));
                lastPos = start;
                steps.add(new Step(null, start));
                first = false;
            } else {
                var pos = lastPos;

                var yDir = 0;

                for (int i = 0; i < sb.length(); i++) {
                    var c = sb.charAt(i);
                    switch (c) {
                        case 'E' -> pos = pos.add(PositionHelper.east());
                        case 'W' -> pos = pos.add(PositionHelper.west());
                        case 'U' -> yDir = 1;
                        case 'D' -> yDir = -1;
                        case '(' -> {
                            var sb2 = new StringBuilder();
                            while (true) {
                                var c2 = sb.charAt(++i);
                                if (c2 == ')') break;
                                sb2.append(c2);
                            }
                            pos = pos.add(up(yDir * Double.parseDouble(sb2.toString())));
                        }
                        case 'S' -> pos = pos.add(PositionHelper.south());
                        case 'N' -> pos = pos.add(PositionHelper.north());
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

    public Vec3d getStart() {
        return start;
    }
}
