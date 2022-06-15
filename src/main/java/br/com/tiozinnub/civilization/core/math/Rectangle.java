package br.com.tiozinnub.civilization.core.math;

import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class Rectangle {
    public final int x;
    public final int z;
    public final int width;
    public final int height;

    public Rectangle(Pos2d pos1, Pos2d pos2) {
        this.x = Math.min(pos1.x, pos2.x);
        this.z = Math.min(pos1.z, pos2.z);
        this.width = Math.abs(pos1.x - pos2.x) + 1;
        this.height = Math.abs(pos1.z - pos2.z) + 1;
    }

    public Rectangle(int x, int z, int width, int height) {
        this.x = x;
        this.z = z;
        this.width = width;
        this.height = height;
    }

    public static Rectangle fromCenter(Pos2d center, int width, int height) {
        return new Rectangle(center.x - width / 2, center.z - height / 2, width, height);
    }

    public static Rectangle fromCenter(Pos2d center, int size) {
        return fromCenter(center, size, size);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int left() {
        return this.x;
    }

    public int right() {
        return this.x + this.width - 1;
    }

    public int top() {
        return this.z;
    }

    public int bottom() {
        return this.z + this.height - 1;
    }

    public boolean isInside(int x, int z) {
        return x >= this.left() && x <= this.right() && z >= this.top() && z <= this.bottom();
    }

    public boolean isInside(Pos2d pos) {
        return isInside(pos.x, pos.z);
    }

    public boolean isInside(int x, int z, int width, int height) {
        return isInside(x, z) && isInside(x + width - 1, z + height - 1);
    }

    public boolean isInside(Rectangle rectangle) {
        return this.isInside(rectangle.getX(), rectangle.getZ(), rectangle.getWidth(), rectangle.getHeight());
    }

    public Rectangle inflate(int distanceX, int distanceZ) {
        return new Rectangle(this.x - distanceX, this.z - distanceZ, this.width + distanceX * 2, this.height + distanceZ * 2);
    }

    public Rectangle inflate(int distance) {
        return this.inflate(distance, distance);
    }

    public Rectangle add(int width, int height) {
        return new Rectangle(this.x, this.z, this.width + width, this.height + height);
    }

    public List<Pos2d> getAllPositions() {
        var positions = new ArrayList<Pos2d>();
        for (var z = this.top(); z <= this.bottom(); z++) {
            for (var x = this.left(); x <= this.right(); x++) {
                positions.add(new Pos2d(x, z));
            }
        }
        return positions;
    }

    public boolean isSquare() {
        return this.width == this.height;
    }

    public Pos2d getCenter() {
        return new Pos2d(this.x + this.width / 2, this.z + this.height / 2);
    }

    public Box getBox(int minY, int maxY) {
        return new Box(this.left(), minY, this.top(), this.right(), maxY, this.bottom());
    }

    @Override
    public String toString() {
        return "Rectangle{x=%d, z=%d, width=%d, height=%d}".formatted(x, z, width, height);
    }
}
