package com.ilife.iliferobot.model.bean;

import com.ilife.iliferobot.entity.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * X800系列绘制格子地图的数据类
 * 保存的是解析过的数据
 */
public class CleaningDataX8 {
    private List<Coordinate> coordinates;
    private int cleanArea;
    private int workTime;
    private boolean haveClearFlag;

    public CleaningDataX8() {
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public int getCleanArea() {
        return cleanArea;
    }

    public void setCleanArea(int cleanArea) {
        this.cleanArea = cleanArea;
    }

    public int getWorkTime() {
        return workTime;
    }

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public boolean isHaveClearFlag() {
        return haveClearFlag;
    }


    public void setHaveClearFlag(boolean haveClearFlag) {
        this.haveClearFlag = haveClearFlag;
        if (coordinates != null) {
            coordinates.clear();
        }
    }

    public void addCoordinate(Coordinate coordinate) {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        }
        if (!coordinates.contains(coordinate)) {
            coordinates.add(coordinate);
        }
    }
}
