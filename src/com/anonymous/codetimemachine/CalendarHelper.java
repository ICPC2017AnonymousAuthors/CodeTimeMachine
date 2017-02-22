package com.anonymous.codetimemachine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarHelper
{
    // General Rule: whenever use cal.get(Calendar.MONTH/HOUR/MIN) which return INT, check to add leading 0 for one digit.
    static String convertDateToString_(Date d)
    {
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date (month/day/year)
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy"); //"MM/dd/yyyy HH:mm:ss"

        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        String reportDate = df.format(today);

        return reportDate;
    }

    static String convertDateToStringYMD(Date d)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        int m = cal.get(Calendar.DAY_OF_MONTH);
        String result = cal.get(Calendar.YEAR)+" "+convertMonthIndexToShortName(cal.get(Calendar.MONTH))+" "+((m<10)?("0"+m):m);
        return result;
    }

    static String convertDateToStringYmDHM(Date d)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        int m = cal.get(Calendar.DAY_OF_MONTH);
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        String result = cal.get(Calendar.YEAR)+" "+convertMonthIndexToShortName(cal.get(Calendar.MONTH))+" "+((m<10)?("0"+m):m)
                        +"   "+((h<10)?("0"+h):h)+":"+((min<10)?("0"+min):min);
        return result;
    }

    static String convertDateToStringYMDHM(Date d)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd  HH:mm");
        return format.format(d);
    }

    static String convertDateToStringMDY(Date d)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        String result = convertMonthIndexToShortName(cal.get(Calendar.MONTH))+" "+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.YEAR);
        return result;
    }

    static String convertMonthIndexToShortName(int index_0based)
    {
        Date d = new Date(2000,index_0based,1);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(d);
        return month_name;
    }

    static boolean isSameDay(Date d1, Date d2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);


        boolean isSameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return isSameDay;
    }

    static String convertDateToTime(Date d)
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm"); //"yyyy-MM-dd HH:mm:ss.SSS"
        return format.format(d);
    }
}
