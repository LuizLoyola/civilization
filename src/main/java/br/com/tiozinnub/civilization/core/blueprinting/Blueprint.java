package br.com.tiozinnub.civilization.core.blueprinting;

public class Blueprint {
    public final int width;
    public final int height;

    public Blueprint(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
