package com.android.samplesmartpen.models;

import android.graphics.RectF;

import java.util.ArrayList;

public class Question {

    private int ID;
    private int QNo;
    private int QType;
    private int PageNum;
    private String Media;
    private String Quest;
    private String Answer;
    private ArrayList<Option> falOptions;
    private RectF qnoCoor;
    private RectF mediaCoor;

    public Question() {
        this.falOptions = new ArrayList<>();
    }

    public Question(int ID, int QNo, int QType, int pageNum, String media, String quest, String answer, ArrayList<Option> falOptions) {
        this.ID = ID;
        this.QNo = QNo;
        this.QType = QType;
        PageNum = pageNum;
        Media = media;
        Quest = quest;
        Answer = answer;

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getQNo() {
        return QNo;
    }

    public void setQNo(int QNo) {
        this.QNo = QNo;
    }

    public int getQType() {
        return QType;
    }

    public void setQType(int QType) {
        this.QType = QType;
    }

    public int getPageNum() {
        return PageNum;
    }

    public void setPageNum(int pageNum) {
        PageNum = pageNum;
    }

    public String getMedia() {
        return Media;
    }

    public void setMedia(String media) {
        Media = media;
    }

    public String getQuest() {
        return Quest;
    }

    public void setQuest(String quest) {
        Quest = quest;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public ArrayList<Option> getFalOptions() {
        return falOptions;
    }

    public void setFalOptions(ArrayList<Option> falOptions) {
        this.falOptions = falOptions;
    }

    public void addOptions(Option option) {
        falOptions.add(option);
    }

    public RectF getQnoCoor() {
        return qnoCoor;
    }

    public void setQnoCoordinate(float x1, float x2, float y1, float y2) {
        qnoCoor = new RectF(x1, y1, x2, y2);
    }

    public RectF getMediaCoor() {
        return mediaCoor;
    }

    public void setMediaCoordinate(float x1, float x2, float y1, float y2) {
        mediaCoor = new RectF(x1, y1, x2, y2);
    }
}
