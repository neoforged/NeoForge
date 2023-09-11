package net.feltmc.neoforge.patches.interfaces;

public interface AbstractSelectionListInterface {
    default public int getWidth() { return 0; }
    default public int getHeight() { return 0; }
    default public int getTop() { return 0; }
    default public int getBottom() { return 0; }
    default public int getLeft() { return 0; }
    default public int getRight() { return 0; }
}
