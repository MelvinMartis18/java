package com.application.essentials;

public class AlarmOptions {

    private  int intervalTime;
    private  int durationTime;
    private int endAtTimeHour;
    private int endAtTimeMinute;

    public AlarmOptions(int intervalTime, int durationTime, int endAtTimeHour, int endAtTimeMinute) {
        this.intervalTime = intervalTime;
        this.durationTime = durationTime;
        this.endAtTimeHour = endAtTimeHour;
        this.endAtTimeMinute = endAtTimeMinute;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public int getEndAtTimeHour() {
        return endAtTimeHour;
    }

    public void setEndAtTimeHour(int endAtTimeHour) {
        this.endAtTimeHour = endAtTimeHour;
    }

    public int getEndAtTimeMinute() {
        return endAtTimeMinute;
    }

    public void setEndAtTimeMinute(int endAtTimeMinute) {
        this.endAtTimeMinute = endAtTimeMinute;
    }

    @Override
    public String toString() {
        return "AlarmOptions{" +
                "intervalTime=" + intervalTime +
                ", durationTime=" + durationTime +
                ", endAtTimeHour=" + endAtTimeHour +
                ", endAtTimeMinute=" + endAtTimeMinute +
                '}';
    }
}
