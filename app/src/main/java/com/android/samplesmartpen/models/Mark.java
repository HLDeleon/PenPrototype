package com.android.samplesmartpen.models;

import android.graphics.Bitmap;

public class Mark {

    private Bitmap bitmap;
    private float CenterX;
    private float CenterY;
    private float left;
    private float top;
    private float right;
    private float bottom;
    private boolean isCorrect;

    public Mark() {
    }

    public Mark(Bitmap bitmap, float centerX, float centerY, float left, float top, float right, float bottom, boolean isCorrect) {
        this.bitmap = bitmap;
        CenterX = centerX;
        CenterY = centerY;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.isCorrect = isCorrect;
    }

    public Mark(Bitmap bitmap, float left, float top) {
        this.bitmap = bitmap;
        this.left = left;
        this.top = top;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float getCenterX() {
        return CenterX;
    }

    public void setCenterX(float centerX) {
        CenterX = centerX;
    }

    public float getCenterY() {
        return CenterY;
    }

    public void setCenterY(float centerY) {
        CenterY = centerY;
    }

    public float getRight() {
        return right;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float getBottom() {
        return bottom;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
