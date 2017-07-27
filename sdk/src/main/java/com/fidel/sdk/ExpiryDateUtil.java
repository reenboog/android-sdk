package com.fidel.sdk;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
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

    public static String monthStringFromExpiryString(String exp) {
        Pattern datePattern = Pattern.compile("(^0[1-9]|1[0-2]) / ([0-9][0-9])");

        Matcher matcher = datePattern.matcher(exp);
        if(matcher.find()) {
            String monthGroup = matcher.group(1);

            return monthGroup;
        }

        return null;
    }

    public static String yearStringFromExpiryString(String exp) {
        Pattern datePattern = Pattern.compile("(^0[1-9]|1[0-2]) / ([0-9][0-9])");

        Matcher matcher = datePattern.matcher(exp);
        if(matcher.find()) {
            String yearGroup = matcher.group(2);

            return yearGroup;
        }

        return null;
    }

    public static int yearInTwoDigitFormat(int year) {
        if(year < 100) {
            return year;
        } else {
            return year % MILLENIUM_BASE;
        }
    }

    public static int yearInFourDigitFormat(int year) {
        if(year < 100) {
            return year + MILLENIUM_BASE;
        } else {
            return year;
        }
    }

    public static int monthFromString(String m) {
        try {
            int month = Integer.parseInt(m);

            return month;
        } catch(NumberFormatException e) {
            Log.d("ExpiryDateUtil.DEBUG", "Failed to parse month: " + m);
        }

        return 0;
    }

    public static int yearFromString(String y) {
        try {
            int year = Integer.parseInt(y);

            return year;
        } catch(NumberFormatException e) {
            Log.d("ExpiryDateUtil.DEBUG", "Failed to parse year: " + y);
        }

        return 0;
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
