package com.android.samplesmartpen.models;

import java.util.ArrayList;

public class Content {

    private int ID;
    private String Directions;
    private String PageRange;
    private ArrayList<Question> falQuestions;

    public Content() {
        this.falQuestions = new ArrayList<>();
    }

    public Content(int ID, String directions) {
        this.ID = ID;
        Directions = directions;
        this.falQuestions = new ArrayList<>();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDirections() {
        return Directions;
    }

    public void setDirections(String directions) {
        Directions = directions;
    }

    public ArrayList<Question> getFalQuestions() {
        return falQuestions;
    }

    public void setFalQuestions(ArrayList<Question> falQuestions) {
        this.falQuestions = falQuestions;
    }

    public String getPageRange() {
        return PageRange;
    }

    public void setPageRange(String pageRange) {
        PageRange = pageRange;
    }

    public void addQuestion(Question question) {
        falQuestions.add(question);
    }

    public int[] getPages() {

        String[] pages = PageRange.split("-");
        int[] pagesinINT = new int[pages.length];

        for (int i = 0; i < pages.length; i++) {
            pagesinINT[i] = Integer.parseInt(pages[i]);
        }

        return pagesinINT;
    }
}
