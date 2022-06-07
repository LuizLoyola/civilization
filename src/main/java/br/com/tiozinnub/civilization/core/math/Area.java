package br.com.tiozinnub.civilization.core.math;


public interface Area {
    boolean contains(int x, int z);

    int getLeft();

    int getRight();

    int getTop();

    int getBottom();
}