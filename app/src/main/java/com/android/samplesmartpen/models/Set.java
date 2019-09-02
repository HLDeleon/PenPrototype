package com.android.samplesmartpen.models;

public class Set {
    private int fiId;
    private int fiPId;
    private String fsName;
    private String fsCode;
    private int fiOrder;

    public Set() {
        super();
        this.fiId = 0;
        this.fiPId = 0;
        this.fsName = "";
        this.fsCode = "";
        this.fiOrder = 0;
    }

    public Set(int fiId, int fiPId, String fsName, String fsCode,
               int fiOrder) {
        super();
        this.fiId = fiId;
        this.fiPId = fiPId;
        this.fsName = fsName;
        this.fsCode = fsCode;
        this.fiOrder = fiOrder;
    }

    public void setFiId(int fiId) {
        this.fiId = fiId;
    }

    public void setFiPId(int fiPId) {
        this.fiPId = fiPId;
    }

    public void setFsName(String fsName) {
        this.fsName = fsName;
    }

    public void setFsCode(String fsCode) {
        this.fsCode = fsCode;
    }

    public void setFiOrder(int fiOrder) {
        this.fiOrder = fiOrder;
    }

    public int getFiId() {
        return fiId;
    }

    public int getFiPId() {
        return fiPId;
    }

    public String getFsName() {
        return fsName;
    }

    public String getFsCode() {
        return fsCode;
    }

    public int getFiOrder() {
        return fiOrder;
    }
}
