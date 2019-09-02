package com.android.samplesmartpen.models;

import java.util.ArrayList;

public class Level extends Set {
    public ArrayList<Set> falSets;

    public Level() {
        super();
        this.falSets = new ArrayList<Set>();
    }

    public Level(int fiId, int fiPId, String fsName, String fsCode, int fiOrder) {
        super(fiId, fiPId, fsName, fsCode, fiOrder);
        this.falSets = new ArrayList<Set>();
    }

    public Level(ArrayList<Set> falSets) {
        super();
        this.falSets = falSets;
    }

    public void setFalSets(ArrayList<Set> falSets) {
        this.falSets = falSets;
    }

    public ArrayList<Set> getFalSets() {
        return falSets;
    }

    public void addSet(Set set) {
        this.falSets.add(set);
    }
}
