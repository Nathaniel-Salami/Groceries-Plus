package com.comp1601.groceriesplus;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ToolBox {

    public static final String DATE_FORMAT_DB = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_UI = "E, MMM dd ";

    static final String GLIST_EXTRA = "glist";
    static final String MODEL_EXTRA = "gModel";

    public static String dateToUiString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_UI);

        String dateString = (date != null) ? format.format(date) : "";
        return dateString;
    }

    public static String dateToDbString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_DB);

        String dateString = (date != null) ? format.format(date) : "";
        return dateString;
    }

    public static Date dbStringToDate(String dateString) {
        Date date = null;

        try {
            date = new SimpleDateFormat(DATE_FORMAT_DB).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date uiStringToDate(String dateString) {
        Date date = null;

        try {
            date = new SimpleDateFormat(DATE_FORMAT_UI).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
