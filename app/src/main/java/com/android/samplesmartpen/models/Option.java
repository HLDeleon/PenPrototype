package com.android.samplesmartpen.models;

import android.graphics.RectF;

public class Option {

    private String option;
    private RectF optionCoordinate;

    public Option() {

    }

    public Option(String option) {
        this.option = option;

    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public RectF getOptionCoordinate() {
        return optionCoordinate;
    }

    public void setOptionCoordinate(float x1, float x2, float y1, float y2) {
        optionCoordinate = new RectF(x1, y1, x2, y2);
    }
}
