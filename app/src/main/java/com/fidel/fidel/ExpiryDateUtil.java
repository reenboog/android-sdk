package com.fidel.fidel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by reenboog on 7/21/17.
 */

public class ExpiryDateUtil {
    public static final String DATE_SPACE = " / ";
    public static final String MONTH_ADDON = "0";

    public static final int MILLENIUM_BASE = 2000;

    public static boolean isStringInValidFormat(String s) {
        Pattern datePattern = Pattern.compile("(^(0[1-9]|1[0-2]) / ([0-9]?[0-9]?))|(^(0[1-9]|1[0-2]) /?)|(^(0[1-9]?|1[0-2]?)$)");

        boolean match = datePattern.matcher(s).matches();

        return match;
    }

    public static String expiryStringForMonthAndYear(int month, int year) {
        year = yearInTwoDigitFormat(year);

        String expiry = monthNumericStringForMonth(month) + DATE_SPACE + year;

        return expiry;
    }

    public static int yearInTwoDigitFormat(int year) {
        if(year < 100) {
            return year;
        } else {
            return year % MILLENIUM_BASE;
        }
    }

    public static String monthNumericStringForMonth(int m) {
        String month = "";

        if(m < 10) {
            month = MONTH_ADDON + m;
        } else {
            month = "" + m;
        }

        return month;
    }

    public static boolean isDateExpired(String s) {
        Pattern datePattern = Pattern.compile("(^(0[1-9]|1[0-2]) / ([0-9][0-9]))");

        boolean match = datePattern.matcher(s).matches();

        if(match) {
            // check if the date has actually expired
            DateFormat format = new SimpleDateFormat("MM" + DATE_SPACE + "yy", Locale.ENGLISH);

            try {
                Date date = format.parse(s);
                Date today = new Date();

                Calendar dateCalendar = Calendar.getInstance();
                Calendar todayCalendar = Calendar.getInstance();

                dateCalendar.setTime(date);
                todayCalendar.setTime(today);

                int dateMaxDay = dateCalendar.getMaximum(Calendar.DAY_OF_MONTH);

                int yearDiff = dateCalendar.get(Calendar.YEAR) - todayCalendar.get(Calendar.YEAR);
                int monthDiff = dateCalendar.get(Calendar.MONTH) - todayCalendar.get(Calendar.MONTH);
                int dayDiff = dateMaxDay - todayCalendar.get(Calendar.DAY_OF_MONTH);

//                return !(yearDiff >= 0 && monthDiff >= 0 && dayDiff >= 0);
                if(yearDiff < 0) {
                    return true;
                } else if(yearDiff == 0) {
                    if(monthDiff < 0) {
                        return true;
                    } else if(monthDiff == 0) {
                        if(dayDiff < 0) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
