package com.ilife.iliferobot.entity;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryRecord_x9 implements Serializable {
    private int work_time;
    private int slam_xMin;
    private int slam_xMax;
    private int slam_yMin;
    private int slam_yMax;
    private int start_reason;
    private int stop_reason;
    private long start_time;
    private int clean_area;
    private ArrayList<String> historyData;
    private String lineSpace;


    public int getSlam_xMin() {
        return slam_xMin;
    }

    public void setSlam_xMin(int slam_xMin) {
        this.slam_xMin = slam_xMin;
    }

    public int getSlam_xMax() {
        return slam_xMax;
    }

    public void setSlam_xMax(int slam_xMax) {
        this.slam_xMax = slam_xMax;
    }

    public int getSlam_yMin() {
        return slam_yMin;
    }

    public void setSlam_yMin(int slam_yMin) {
        this.slam_yMin = slam_yMin;
    }

    public int getSlam_yMax() {
        return slam_yMax;
    }

    public void setSlam_yMax(int slam_yMax) {
        this.slam_yMax = slam_yMax;
    }

    public int getStart_reason() {
        return start_reason;
    }

    public void setStart_reason(int start_reason) {
        this.start_reason = start_reason;
    }

    public int getStop_reason() {
        return stop_reason;
    }

    public void setStop_reason(int stop_reason) {
        this.stop_reason = stop_reason;
    }



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
