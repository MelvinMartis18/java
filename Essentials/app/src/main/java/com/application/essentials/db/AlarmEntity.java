package com.application.essentials.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.application.essentials.AlarmOptions;

import java.util.Arrays;
import java.util.List;

@Entity
public class AlarmEntity {

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "alarmId")
    int alarmId;


    @ColumnInfo(name = "hourOfDay")
    int hourOfDay;

    @ColumnInfo(name = "minute")
    int minute;

    @ColumnInfo(name = "label")
    String label;

    @ColumnInfo(name = "alarmOn")
    Boolean alarmOn;

    @ColumnInfo(name = "repeatWeekly")
    Boolean repeatWeekly;

    @TypeConverters(Converter.class)
    @ColumnInfo(name = "days")
    List<Integer> daysArr;

    @ColumnInfo(name = "intervalTime")
    int intervalTime;

    @ColumnInfo(name = "durationTime")
    int durationTime;

    @ColumnInfo(name = "endAtTimeHour")
    int endAtTimeHour;

    @ColumnInfo(name = "endAtTimeMinute")
    int endAtTimeMinute;

    @ColumnInfo(name = "lastAlarmTimeInMillis")
    long lastAlarmTimeInMillis;

    @ColumnInfo(name = "alarmStartTimeInMillis")
    long alarmStartTimeInMillis;

    @ColumnInfo(name = "alarmEndTimeInMillis")
    long alarmEndTimeInMillis;

    @ColumnInfo(name = "vibrate")
    Boolean vibrate;

    @ColumnInfo(name = "upcomingAlarmTime")
    int upcomingAlarmTime;

    @ColumnInfo(name = "ascendingVolumeMax")
    int ascendingVolumeMax;


    public AlarmEntity(int alarmId, int hourOfDay, int minute, String label, Boolean alarmOn, Boolean repeatWeekly, List<Integer> daysArr, int intervalTime, int durationTime, int endAtTimeHour, int endAtTimeMinute, long alarmStartTimeInMillis, long alarmEndTimeInMillis, Boolean vibrate, int upcomingAlarmTime, int ascendingVolumeMax) {
        this.alarmId = alarmId;
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.label = label;
        this.alarmOn = alarmOn;
        this.repeatWeekly = repeatWeekly;
        this.daysArr = daysArr;
        this.intervalTime = intervalTime;
        this.durationTime = durationTime;
        this.endAtTimeHour = endAtTimeHour;
        this.endAtTimeMinute = endAtTimeMinute;
        this.alarmStartTimeInMillis = alarmStartTimeInMillis;
        this.alarmEndTimeInMillis = alarmEndTimeInMillis;
        this.vibrate = vibrate;
        this.upcomingAlarmTime = upcomingAlarmTime;
        this.ascendingVolumeMax = ascendingVolumeMax;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Integer> getDaysArr() { return daysArr; }

    public void setDaysArr(List<Integer> daysArr) {
        this.daysArr = daysArr;
    }

    public int getAlarmId() {
        return alarmId;
    }

    private void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public Boolean getAlarmOn() {
        return alarmOn;
    }

    public void setAlarmOn(Boolean alarmOn) {
        this.alarmOn = alarmOn;
    }

    public Boolean getRepeatWeekly() {  return repeatWeekly; }

    public void setRepeatWeekly(Boolean repeatWeekly) {  this.repeatWeekly = repeatWeekly; }

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

    public long getLastAlarmTimeInMillis() {
        return lastAlarmTimeInMillis;
    }

    public void setLastAlarmTimeInMillis(long lastAlarmTimeInMillis) {
        this.lastAlarmTimeInMillis = lastAlarmTimeInMillis;
    }

    public long getAlarmStartTimeInMillis() { return alarmStartTimeInMillis; }

    public void setAlarmStartTimeInMillis(long alarmStartTimeInMillis) {
        this.alarmStartTimeInMillis = alarmStartTimeInMillis; }

    public long getAlarmEndTimeInMillis() {
        return alarmEndTimeInMillis;
    }

    public void setAlarmEndTimeInMillis(long alarmEndTimeInMillis) {
        this.alarmEndTimeInMillis = alarmEndTimeInMillis;
    }

    public Boolean getVibrate() { return vibrate; }

    public void setVibrate(Boolean vibrate) { this.vibrate = vibrate; }

    public int getUpcomingAlarmTime() { return upcomingAlarmTime; }

    public void setUpcomingAlarmTime(int upcomingAlarmTime) {  this.upcomingAlarmTime = upcomingAlarmTime;  }

    public int getAscendingVolumeMax() { return ascendingVolumeMax; }

    public void setAscendingVolumeMax(int ascendingVolumeMax) { this.ascendingVolumeMax = ascendingVolumeMax; }


    @Override
    public String toString() {
        return "AlarmEntity{" +
                "alarmId=" + alarmId +
                ", hourOfDay=" + hourOfDay +
                ", minute=" + minute +
                ", label='" + label + '\'' +
                ", alarmOn=" + alarmOn +
                ", repeatWeekly=" + repeatWeekly +
                ", daysArr=" + daysArr +
                ", intervalTime=" + intervalTime +
                ", durationTime=" + durationTime +
                ", endAtTimeHour=" + endAtTimeHour +
                ", endAtTimeMinute=" + endAtTimeMinute +
                ", lastAlarmTimeInMillis=" + lastAlarmTimeInMillis +
                ", alarmStartTimeInMillis=" + alarmStartTimeInMillis +
                ", alarmEndTimeInMillis=" + alarmEndTimeInMillis +
                ", vibrate=" + vibrate +
                ", upcomingAlarmTime=" + upcomingAlarmTime +
                ", ascendingVolumeMax=" + ascendingVolumeMax +
                '}';
    }
}