package com.android.samplesmartpen.models;

import java.util.ArrayList;

public class Book extends Set {
    public ArrayList<Category> falCategories;

    public Book() {
        super();
        this.falCategories = new ArrayList<Category>();
    }

    public Book(int fiId, int fiPId, String fsName, String fsCode, int fiOrder) {
        super(fiId, fiPId, fsName, fsCode, fiOrder);

    }

    public Book(ArrayList<Category> falCategories) {
        super();
        this.falCategories = falCategories;
    }

    public void setFalCategories(ArrayList<Category> falCategories) {
        this.falCategories = falCategories;
    }

    public ArrayList<Category> getFalCategories() {
        return falCategories;
    }

    public void addCategory(Category category) {
        this.falCategories.add(category);
    }
}
