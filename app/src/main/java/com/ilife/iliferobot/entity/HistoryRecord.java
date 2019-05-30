package com.ilife.iliferobot.entity;


import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryRecord {
    private int work_time;
    private long start_time;
    private int clean_area;
    private ArrayList<String> historyData;
    private String lineSpace;

    public int getWork_time() {
        return work_time;
    }

    public void setWork_time(int work_time) {
        this.work_time = work_time;
    }

    public String getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(String lineSpace) {
        this.lineSpace = lineSpace;
    }

    public ArrayList<String> getHistoryData() {
        return historyData;
    }

    public void setHistoryData(ArrayList<String> historyData) {
        this.historyData = historyData;
    }

    public int getClean_area() {
        return clean_area;
    }

    public void setClean_area(int clean_area) {
        this.clean_area = clean_area;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    @Override
    public String toString() {
        return "HistoryRecord{" +
                "work_time='" + work_time + '\'' +
                ", start_time='" + start_time + '\'' +
                ", clean_area='" + clean_area + '\'' +
                ", historyData=" + historyData +
                ", lineSpace='" + lineSpace + '\'' +
                '}';
    }


}
