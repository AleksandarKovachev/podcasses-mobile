package com.podcasses.database;

import java.util.Date;

import androidx.room.TypeConverter;

/**
 * Created by aleksandar.kovachev.
 */
public class DateConverter {

    @TypeConverter
    public static Date toDate(Long dateLong) {
        return dateLong == null ? null : new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

}
