package com.android.samplesmartpen.models;

import java.util.ArrayList;

public class Category extends Book {
    public ArrayList<Level> falLevels;

    public Category() {
        super();
        this.falLevels = new ArrayList<Level>();
    }

    public Category(int fiId, int fiPId, String fsName, String fsCode,
                    int fiOrder) {
        super(fiId, fiPId, fsName, fsCode, fiOrder);

    }

    public Category(ArrayList<Level> falLevels) {
        super();
        this.falLevels = falLevels;
    }

    public void setFalLevels(ArrayList<Level> falLevels) {
        this.falLevels = falLevels;
    }

    public ArrayList<Level> getFalLevels() {
        return falLevels;
    }

    public void addLevel(Level level) {
        this.falLevels.add(level);
    }
}
