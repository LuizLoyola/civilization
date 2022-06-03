package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.utils.Serializable;

public final class LayoutDimensions extends Serializable {
    private int cityBlockSize;
    private int streetWidth;

    public LayoutDimensions() {
    }

    public LayoutDimensions(int cityBlockSize, int streetWidth) {
        this.cityBlockSize = cityBlockSize;
        this.streetWidth = streetWidth;
    }

    public int getNodeRadius() {
        return (this.streetWidth - 1) / 2;
    }

    public int getNodeDistance() {
        return this.cityBlockSize + this.streetWidth;
    }

    public int getCityBlockSize() {
        return this.cityBlockSize;
    }

    public int getStreetWidth() {
        return this.streetWidth;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("cityBlockSize", this::getCityBlockSize, p -> this.cityBlockSize = p, 0);
        helper.registerProperty("streetWidth", this::getStreetWidth, p -> this.streetWidth = p, 0);
    }
}
