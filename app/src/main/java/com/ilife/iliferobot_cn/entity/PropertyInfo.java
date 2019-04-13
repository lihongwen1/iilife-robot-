package com.ilife.iliferobot_cn.entity;

/**
 * Created by chenjiaping on 2017/8/25.
 */

public class PropertyInfo {
    private int battery_level;
    private int cleaning_cleaning;
    private int error_info;
    private int light_mode;
    private int room_pattern;
    private int sweeping_mode;
    private int vacuum_cleaning;
    private int voice_mode;
    private int work_pattern;

    public int getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(int battery_level) {
        this.battery_level = battery_level;
    }

    public int getWork_pattern() {
        return work_pattern;
    }

    public void setWork_pattern(int work_pattern) {
        this.work_pattern = work_pattern;
    }

    public int getVoice_mode() {
        return voice_mode;
    }

    public void setVoice_mode(int voice_mode) {
        this.voice_mode = voice_mode;
    }

    public int getVacuum_cleaning() {
        return vacuum_cleaning;
    }

    public void setVacuum_cleaning(int vacuum_cleaning) {
        this.vacuum_cleaning = vacuum_cleaning;
    }

    public int getSweeping_mode() {
        return sweeping_mode;
    }

    public void setSweeping_mode(int sweeping_mode) {
        this.sweeping_mode = sweeping_mode;
    }

    public int getRoom_pattern() {
        return room_pattern;
    }

    public void setRoom_pattern(int room_pattern) {
        this.room_pattern = room_pattern;
    }

    public int getLight_mode() {
        return light_mode;
    }

    public void setLight_mode(int light_mode) {
        this.light_mode = light_mode;
    }

    public int getError_info() {
        return error_info;
    }

    public void setError_info(int error_info) {
        this.error_info = error_info;
    }

    public int getCleaning_cleaning() {
        return cleaning_cleaning;
    }

    public void setCleaning_cleaning(int cleaning_cleaning) {
        this.cleaning_cleaning = cleaning_cleaning;
    }

    @Override
    public String toString() {
        return "PropertyInfo{" +
                "battery_level=" + battery_level +
                ", cleaning_cleaning=" + cleaning_cleaning +
                ", error_info=" + error_info +
                ", light_mode=" + light_mode +
                ", room_pattern=" + room_pattern +
                ", sweeping_mode=" + sweeping_mode +
                ", vacuum_cleaning=" + vacuum_cleaning +
                ", voice_mode=" + voice_mode +
                ", work_pattern=" + work_pattern +
                '}';
    }
}
