package br.com.tiozinnub.civilization.core.math;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MapArea extends Serializable implements Area {
    private int width;
    private int height;
    private boolean[][] matrix;
    private int x;
    private int z;

    public MapArea(Box box) {
        this((int) box.minX, (int) box.minZ, (int) box.getXLength(), (int) box.getZLength());
    }

    public MapArea() {
        this(0, 0, 0, 0);
    }

    public MapArea(int x, int z, int width, int height) {
        this.x = x;
        this.z = z;
        this.width = width;
        this.height = height;

        this.matrix = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.matrix[i][j] = true;
            }
        }
    }

    public boolean contains(Rectangle rect) {
        return contains(rect.getX(), rect.getZ(), rect.getWidth(), rect.getHeight());
    }

    public boolean contains(int x, int z, int width, int height) {
        if (x < this.x || z < this.z || x + width > this.x + this.width || z + height > this.z + this.height) {
            return false;
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!this.matrix[i + x - this.x][j + z - this.z]) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int getLeft() {
        return this.x;
    }

    @Override
    public int getRight() {
        return this.x + this.width;
    }

    @Override
    public int getTop() {
        return this.z;
    }

    @Override
    public int getBottom() {
        return this.z + this.height;
    }

    public MapArea getChunkArea() {
        var startChunk = new ChunkPos(new BlockPos(this.x, 0, this.z));
        var endChunk = new ChunkPos(new BlockPos(this.x + this.width, 0, this.z + this.height));
        var chunkArea = new MapArea(startChunk.x, startChunk.z, endChunk.x - startChunk.x + 1, endChunk.z - startChunk.z + 1);

        for (var x = this.getLeft(); x < this.getRight(); x++) {
            for (var z = this.getTop(); z < this.getBottom(); z++) {
                if (this.contains(x, z)) {
                    var blockPos = new BlockPos(x, 0, z);
                    var chunkPos = new ChunkPos(blockPos);
                    chunkArea.matrix[chunkPos.x - chunkArea.x][chunkPos.z - chunkArea.z] = true;
                }
            }
        }

        return chunkArea;
    }

    public MapArea add(Area area) {
        var minX = Math.min(this.getLeft(), area.getLeft());
        var minZ = Math.min(this.getTop(), area.getTop());
        var maxX = Math.max(this.getRight(), area.getRight());
        var maxZ = Math.max(this.getBottom(), area.getBottom());

        var newWidth = maxX - minX;
        var newHeight = maxZ - minZ;

        var newArea = new MapArea(minX, minZ, newWidth, newHeight);

        for (var x = minX; x < maxX; x++) {
            for (var z = minZ; z < maxZ; z++) {
                newArea.matrix[x - minX][z - minZ] = (this.contains(x, z) && this.matrix[x - this.x][z - this.z]) || area.contains(x, z);
            }
        }

        return newArea;
    }

    public MapArea subtract(Area area) {
        for (var x = area.getLeft(); x < area.getRight(); x++) {
            for (var z = area.getTop(); z < area.getBottom(); z++) {
                if (this.contains(x, z)) {
                    this.matrix[x - this.x][z - this.z] = false;
                }
            }
        }

        return simplify();
    }

    private MapArea simplify() {
        var matrixString = this.getMatrixString();
        if (matrixString.equals("")) {
            return new MapArea(0, 0, 0, 0);
        }

        var lines = Arrays.stream(matrixString.split("\n")).toList();

        // remove first empty lines
        while (lines.get(0).trim().equals("")) {
            lines = lines.subList(1, lines.size());
        }

        // remove last empty lines
        while (lines.get(lines.size() - 1).trim().equals("")) {
            lines = lines.subList(0, lines.size() - 1);
        }

        // remove all empty first chars
        while (lines.stream().allMatch(line -> line.startsWith(" "))) {
            lines = lines.stream().map(line -> line.substring(1)).toList();
        }

        // remove all empty last chars
        while (lines.stream().allMatch(line -> line.endsWith(" "))) {
            lines = lines.stream().map(line -> line.substring(0, line.length() - 1)).toList();
        }

        var newArea = new MapArea(this.x, this.z, lines.get(0).length(), lines.size());
        for (var z = 0; z < lines.size(); z++) {
            for (var x = 0; x < lines.get(z).length(); x++) {
                newArea.matrix[x][z] = lines.get(z).charAt(x) != ' ';
            }
        }

        return newArea;
    }

    public String getMatrixString() {
        var sb = new StringBuilder();

        if (this.height == 0 || this.width == 0) {
            return null;
        }

        for (var z = 0; z < this.height; z++) {
            for (var x = 0; x < this.width; x++) {
                if (this.matrix.length > x && this.matrix[x].length > z) {
                    sb.append(this.matrix[x][z] ? "." : " ");
                } else {
                    sb.append(" ");
                }
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    private void fromMatrixString(String matrixString) {
        if (matrixString == null || matrixString.equals("")) {
            this.width = 0;
            this.height = 0;
            this.matrix = new boolean[0][0];
            return;
        }

        // check size
        var lines = Arrays.stream(matrixString.split("\n")).toList();

        // check if all lines are the same size
        if (lines.stream().map(String::length).distinct().count() != 1) {
            throw new IllegalArgumentException("MapArea matrix must be rectangular");
        }

        this.width = lines.get(0).length();
        this.height = lines.size();
        this.matrix = new boolean[this.width][this.height];

        for (var i = 0; i < this.width; i++) {
            for (var j = 0; j < this.height; j++) {
                this.matrix[i][j] = lines.get(j).charAt(i) != ' ';
            }
        }
    }

    @Override
    public boolean contains(int x, int z) {
        if (this.width == 0 || this.height == 0) {
            return false;
        }
        return x >= this.x && x < this.x + this.width && z >= this.z && z < this.z + this.height && this.matrix[x - this.x][z - this.z];
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("x", () -> this.x, x -> this.x = x, 0);
        helper.registerProperty("z", () -> this.z, z -> this.z = z, 0);
        helper.registerProperty("width", () -> this.width, width -> this.width = width, 0);
        helper.registerProperty("height", () -> this.height, height -> this.height = height, 0);
        helper.registerProperty("matrix", this::getMatrixString, matrixString -> {
            if (this.width != 0 && this.height != 0) fromMatrixString(matrixString);
        }, "");
    }

    public MapArea inflate(int i) {
        return inflate(i, i);
    }

    public MapArea inflate(int x, int z) {
        var newArea = new MapArea(this.x - x, this.z - z, this.width + x * 2, this.height + z * 2);

        if (this.width != 0 && this.height != 0) {
            for (int offsetX = -x; offsetX <= x; offsetX++) {
                for (int offsetZ = -z; offsetZ <= z; offsetZ++) {
                    if (offsetX == 0 && offsetZ == 0) continue;

                    for (int i = 0; i < this.width; i++) {
                        for (int j = 0; j < this.height; j++) {
                            newArea.matrix[i + offsetX + x][j + offsetZ + z] |= this.matrix[i][j];
                        }
                    }
                }
            }
        }

        return newArea;
    }

    public List<Rectangle> fitRectangle(int width, int height, boolean mayRotate) {

        if (mayRotate) {
            var list = new ArrayList<>(fitRectangle(width, height, false));
            if (width != height) {
                //noinspection SuspiciousNameCombination
                list.addAll(fitRectangle(height, width, false));
            }
            // remove duplicates
            return list.stream().distinct().collect(Collectors.toList());
        } else {
            return fitRectangle(width, height);
        }

    }

    private List<Rectangle> fitRectangle(int width, int height) {
        var list = new ArrayList<Rectangle>();

        for (var x = this.getLeft(); x < this.getRight() - width; x++) {
            for (var z = this.getTop(); z < this.getBottom() - height; z++) {
                var fits = true;

                for (var i = 0; i < width; i++) {
                    for (var j = 0; j < height; j++) {
                        if (!this.contains(x + i, z + j)) {
                            fits = false;
                            break;
                        }
                    }
                    if (!fits) break;
                }

                if (fits) {
                    list.add(new Rectangle(x, z, width, height));
                }
            }
        }

        return list;
    }
}
