package br.com.tiozinnub.civilization.core.math;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

import static java.lang.System.arraycopy;

public abstract class Area2d<EnumType extends Enum<EnumType>> extends Serializable {
    private static final char EMPTY_CHAR = (char) 0;
    private final Map<Character, EnumType> charMap;
    private final Class<EnumType> enumTypeClass;
    private char[][] matrix;
    private int matrixWidth;
    private int matrixHeight;
    private int offsetX;
    private int offsetZ;

    protected Area2d(Map<Character, EnumType> charMap, Class<EnumType> enumTypeClass) {
        this.charMap = charMap;
        this.enumTypeClass = enumTypeClass;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("offsetX", this::getOffsetX, (value) -> this.offsetX = value, 0);
        helper.registerProperty("offsetZ", this::getOffsetZ, (value) -> this.offsetZ = value, 0);
        helper.registerProperty("matrixWidth", () -> this.matrixWidth, (value) -> this.matrixWidth = value, 0);
        helper.registerProperty("matrixHeight", () -> this.matrixHeight, (value) -> this.matrixHeight = value, 0);
        helper.registerProperty("matrix", this::serializeMatrix, this::deserializeMatrix, null);
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    private int left() {
        return offsetX;
    }

    private int right() {
        return offsetX + matrixWidth - 1;
    }

    private int top() {
        return offsetZ;
    }

    private int bottom() {
        return offsetZ + matrixHeight - 1;
    }

    protected String serializeMatrix() {
        if (matrix == null) return null;
        if (matrixWidth == 0 || matrixHeight == 0) return null;

        var sb = new StringBuilder();
        for (var row = 0; row < matrixHeight; row++) {
            for (var col = 0; col < matrixWidth; col++) {
                var c = matrix[row][col];
                if (c == EMPTY_CHAR) c = ' ';
                sb.append(c);
            }

            if (row < matrixHeight - 1) sb.append('\n');
        }
        return sb.toString();
    }

    private void deserializeMatrix(String data) {
        if (data == null || data.isEmpty()) return;

        var lines = data.split("\n");
        matrixHeight = lines.length;
        matrixWidth = lines[0].length();
        matrix = new char[matrixHeight][matrixWidth];

        for (var row = 0; row < matrixHeight; row++) {
            var line = lines[row];
            for (var col = 0; col < matrixWidth; col++) {
                var c = line.charAt(col);
                if (c == ' ') c = EMPTY_CHAR;
                matrix[row][col] = c;
            }
        }
    }

    protected EnumType getEmpty() {
        return null;
    }

    public EnumType get(int x, int z) {
        if (matrix == null) return getEmpty();
        if (x < left() || x > right()) return getEmpty();
        if (z < top() || z > bottom()) return getEmpty();
        return getValueFor(matrix[z - top()][x - left()]);
    }

    public EnumType get(Pos2d pos) {
        return get(pos.x, pos.z);
    }

    private EnumType getValueFor(char c) {
        return charMap.get(c);
    }

    public void set(int x, int z, EnumType value) {
        set(List.of(new Pos2d(x, z)), value);
    }

    public void set(Rectangle rectangle, EnumType value) {
        set(rectangle.getAllPositions(), value);
    }

    public void set(List<Pos2d> positions, EnumType value) {
        if (!positions.isEmpty()) {
            var minX = Integer.MAX_VALUE;
            var minZ = Integer.MAX_VALUE;
            var maxX = Integer.MIN_VALUE;
            var maxZ = Integer.MIN_VALUE;

            for (var pos : positions) {
                if (pos.x < minX) minX = pos.x;
                if (pos.z < minZ) minZ = pos.z;
                if (pos.x > maxX) maxX = pos.x;
                if (pos.z > maxZ) maxZ = pos.z;
            }

            expandMatrixTo(minX, minZ);
            expandMatrixTo(maxX, maxZ);

            for (var pos : positions) {
                matrix[pos.z - offsetZ][pos.x - offsetX] = getCharFor(value);
            }
        }

        optimizeMatrix();
    }

    public void set(int x, int z, int width, int height, EnumType value) {
        set(new Rectangle(x, z, width, height), value);
    }

    public void inflate(EnumType value, int amount, boolean onlyEmpty) {
        inflate(value, amount, onlyEmpty, value);
    }

    public void inflate(EnumType value, int amount, boolean onlyEmpty, EnumType inflateWith) {
        inflate(EnumSet.of(value), amount, onlyEmpty ? EnumSet.noneOf(enumTypeClass) : EnumSet.allOf(enumTypeClass), true, inflateWith);
    }

    public void inflate(Pos2d pos, int amount, boolean onlyEmpty, EnumType inflateWith) {
        inflate(List.of(pos), amount, onlyEmpty ? EnumSet.noneOf(enumTypeClass) : EnumSet.allOf(enumTypeClass), true, inflateWith);
    }

    public void inflate(EnumType valueToInflate, int amount, EnumSet<EnumType> overwriteWhiteList, boolean includeEmptyOnWhitelist, EnumType inflateWith) {
        inflate(EnumSet.of(valueToInflate), amount, overwriteWhiteList, includeEmptyOnWhitelist, inflateWith);
    }

    public void inflate(EnumSet<EnumType> valuesToInflate, int amount, EnumType overwriteOnly, boolean includeEmptyOnWhitelist, EnumType inflateWith) {
        inflate(valuesToInflate, amount, EnumSet.of(overwriteOnly), includeEmptyOnWhitelist, inflateWith);
    }

    public void inflate(EnumSet<EnumType> valuesToInflate, int amount, EnumSet<EnumType> overwriteWhitelist, boolean includeEmptyOnWhitelist, EnumType inflateWith) {
        inflate(getPositionsWith(valuesToInflate), amount, overwriteWhitelist, includeEmptyOnWhitelist, inflateWith);
    }

    private void inflate(List<Pos2d> positions, int amount, EnumSet<EnumType> overwriteWhitelist, boolean includeEmptyOnWhitelist, EnumType inflateWith) {
        if (amount <= 0) return;

        var positionsToSet = new ArrayList<Pos2d>();

        for (var position : positions) {
            for (var zOffset = -amount; zOffset <= amount; zOffset++) {
                for (var xOffset = -amount; xOffset <= amount; xOffset++) {
                    var x = position.x + xOffset;
                    var z = position.z + zOffset;
                    var value = get(x, z);
                    if (value == getEmpty()) {
                        if (!includeEmptyOnWhitelist) continue;
                        // is empty and should be included
                    } else if (!overwriteWhitelist.contains(value)) {
                        // not empty and not in the whitelist
                        continue;
                    }
                    positionsToSet.add(new Pos2d(x, z));
                }
            }
        }

        set(positionsToSet, inflateWith);
    }

    public boolean isEmpty(int x, int z) {
        if (matrix == null) return true;
        if (x < offsetX || x >= offsetX + matrixWidth) return true;
        if (z < offsetZ || z >= offsetZ + matrixHeight) return true;
        return matrix[z - offsetZ][x - offsetX] == EMPTY_CHAR;
    }

    public boolean isEmpty(Pos2d position) {
        return isEmpty(position.x, position.z);
    }

    public List<Pos2d> getPositionsWith(EnumSet<EnumType> values) {
        return getPositions().stream().filter(pos -> values.contains(get(pos.x, pos.z))).toList();
    }

    public List<Pos2d> getPositionsWith(EnumType value) {
        return getPositionsWith(EnumSet.of(value));
    }

    private List<Pos2d> getPositions(boolean empty) {
        var positions = new ArrayList<Pos2d>();

        for (var x = offsetX; x < offsetX + matrixWidth; x++) {
            for (var z = offsetZ; z < offsetZ + matrixHeight; z++) {
                if (empty == isEmpty(x, z)) {
                    positions.add(new Pos2d(x, z));
                }
            }
        }

        return positions;
    }

    public List<Pos2d> getEmptyPositions() {
        return getPositions(true);
    }

    public List<Pos2d> getPositions() {
        return getPositions(false);
    }

    public List<ChunkPos> getChunks() {
        var chunkSet = new HashSet<ChunkPos>();
        getPositions().forEach(pos -> chunkSet.add(new ChunkPos(pos.asBlockPos())));
        return chunkSet.stream().toList();
    }

    private char getCharFor(EnumType value) {
        if (value == null) return ' ';
        for (var entry : charMap.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Value " + value + " not found in char map");
    }

    private void optimizeMatrix() {
        // FIXME: this is lazy and inefficient
        var text = serializeMatrix();

        if (text == null) {
            matrix = null;
            matrixWidth = 0;
            matrixHeight = 0;
            offsetX = 0;
            offsetZ = 0;
            return;
        }

        // remove empty lines
        var lines = Arrays.stream(text.split("\n")).toList();

        var removedLeft = 0;
        var removedTop = 0;

        while (!lines.isEmpty() && lines.get(0).trim().equals("")) {
            lines = lines.subList(1, lines.size());
            removedTop++;
        }

        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().equals("")) {
            lines = lines.subList(0, lines.size() - 1);
        }

        while (!lines.isEmpty() && lines.stream().allMatch(line -> line.charAt(0) == ' ')) {
            lines = lines.stream().map(line -> line.substring(1)).toList();
            removedLeft++;
        }

        while (!lines.isEmpty() && lines.stream().allMatch(line -> line.charAt(line.length() - 1) == ' ')) {
            lines = lines.stream().map(line -> line.substring(0, line.length() - 1)).toList();
        }

        var newText = String.join("\n", lines);
        deserializeMatrix(newText);
        offsetX += removedLeft;
        offsetZ += removedTop;
    }

    private void expandMatrixTo(int x, int z) {
        if (matrix == null) {
            // no matrix, just create a new 1x1 with offset
            offsetX = x;
            offsetZ = z;
            matrixWidth = 1;
            matrixHeight = 1;
            matrix = new char[1][1];
            return;
        }

        if (x >= left() && x <= right() && z >= top() && z <= bottom()) return;

        // matrix exists, but is not at the right position

        var expandLeft = Math.max(0, left() - x);
        var expandRight = Math.max(0, x - right());
        var expandTop = Math.max(0, top() - z);
        var expandBottom = Math.max(0, z - bottom());

        // expand horizontally
        if (expandLeft + expandRight > 0) {
            for (var zz = 0; zz < matrixHeight; zz++) {
                var line = matrix[zz];
                var newLine = new char[line.length + expandLeft + expandRight];
                arraycopy(line, 0, newLine, expandLeft, line.length);
                matrix[zz] = newLine;
            }
            matrixWidth += expandLeft + expandRight;
            offsetX -= expandLeft;
        }

        // expand vertically
        if (expandTop + expandBottom > 0) {
            var newMatrix = new char[matrixHeight + expandTop + expandBottom][matrixWidth];
            arraycopy(matrix, 0, newMatrix, expandTop, matrixHeight);
            matrix = newMatrix;
            matrixHeight += expandTop + expandBottom;
            offsetZ -= expandTop;
        }
    }

    public List<Rectangle> findRectangles(int width, int height, EnumType value) {
        return findRectangles(width, height, EnumSet.of(value), true);
    }

    public List<Rectangle> findRectangles(int width, int height, EnumType value, boolean mayRotate) {
        return findRectangles(width, height, EnumSet.of(value), mayRotate);
    }

    public List<Rectangle> findRectangles(int width, int height, EnumSet<EnumType> values) {
        return findRectangles(width, height, values, true);
    }

    public List<Rectangle> findRectangles(int width, int height, EnumSet<EnumType> values, boolean mayRotate) {
        var rectangles = new ArrayList<Rectangle>();
        var positions = getPositionsWith(values);
        for (var pos : positions) {
            var rect = new Rectangle(pos.x, pos.z, width, height);
            if (rect.getAllPositions().stream().allMatch(pos2 -> values.contains(get(pos2))))
                rectangles.add(rect);
        }

        if (mayRotate && width != height) {
            //noinspection SuspiciousNameCombination
            rectangles.addAll(findRectangles(height, width, values, false));
        }

        return rectangles;
    }

    public void remove(EnumType value) {
        replace(getCharFor(value), EMPTY_CHAR);
        optimizeMatrix();
    }

    public void replace(char oldChar, char newChar) {
        for (var x = 0; x < matrixWidth; x++) {
            for (var z = 0; z < matrixHeight; z++) {
                if (matrix[z][x] == oldChar) {
                    matrix[z][x] = newChar;
                }
            }
        }
    }

    public boolean hasAny(EnumType value) {
        return hasAny(EnumSet.of(value));
    }

    public boolean hasAny(EnumSet<EnumType> values) {
        var chars = values.stream().map(this::getCharFor).toList();
        for (var x = 0; x < matrixWidth; x++) {
            for (var z = 0; z < matrixHeight; z++) {
                if (chars.contains(matrix[z][x])) {
                    return true;
                }
            }
        }
        return false;
    }
}
