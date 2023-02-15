package br.com.tiozinnub.civilization.block.building.interfaces;

public interface LeveledBuilding {
    int getMaxLevel();

    int getLevel();

    void setLevel(int level);
}
