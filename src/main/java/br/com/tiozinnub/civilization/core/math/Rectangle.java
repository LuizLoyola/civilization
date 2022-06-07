package br.com.tiozinnub.civilization.core.math;

import net.minecraft.util.math.BlockPos;

public final class Rectangle implements Area {
    private final int x;
    private final int z;
    private final int width;
    private final int height;

    public Rectangle(int x, int z, int width, int height) {
        this.x = x;
        this.z = z;
        this.width = width;
        this.height = height;
    }

    public Rectangle(BlockPos pos) {
        this(pos.getX(), pos.getZ(), 1, 1);
    }

    @Override
    public boolean contains(int x, int z) {
        return x >= this.x && x < this.x + this.width && z >= this.z && z < this.z + this.height;
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

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Rectangle) obj;
        return this.x == that.x &&
                this.z == that.z &&
                this.width == that.width &&
                this.height == that.height;
    }

}