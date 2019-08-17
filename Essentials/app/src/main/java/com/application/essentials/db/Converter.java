package com.application.essentials.db;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converter {
    @TypeConverter
    public static String fromList(List<Integer> daysArr) {
        return daysArr.isEmpty() ? null : daysArr.toString().replace("[","")
                .replace("]","")
                .replace(",","")
                .replace(" ","");
    }

    @TypeConverter
    public static List<Integer> fromString(String daysStr) {
        List<Integer> listInt = new ArrayList<Integer>();
        if (daysStr == null){
            listInt = Arrays.asList();
        }
        else {
            for (int i = 0; i < daysStr.length(); i++) {
                listInt.add(Integer.parseInt(daysStr.substring(i,i+1)));  }
        }
        return listInt;
    }
}