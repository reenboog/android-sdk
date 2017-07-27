package com.fidel.sdk;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void monthFromString_isCorrect() throws Exception {
        String m = "01";

        assertEquals(1, ExpiryDateUtil.monthFromString(m));

        m = "00";

        assertEquals(0, ExpiryDateUtil.monthFromString(m));

        m = "8";

        assertEquals(8, ExpiryDateUtil.monthFromString(m));

        m = "12";

        assertEquals(12, ExpiryDateUtil.monthFromString(m));
    }

    @Test
    public void yearFromString_isCorrect() throws Exception {
        String y = "00";

        assertEquals(0, ExpiryDateUtil.yearFromString(y));

        y = "01";

        assertEquals(1, ExpiryDateUtil.yearFromString(y));

        y = "2";

        assertEquals(2, ExpiryDateUtil.yearFromString(y));

        y = "22";

        assertEquals(22, ExpiryDateUtil.yearFromString(y));

        y = "1946";

        assertEquals(1946, ExpiryDateUtil.yearFromString(y));

        y = "2000";

        assertEquals(2000, ExpiryDateUtil.yearFromString(y));

        y = "2019";

        assertEquals(2019, ExpiryDateUtil.yearFromString(y));

        y = "2030";

        assertEquals(2030, ExpiryDateUtil.yearFromString(y));

        y = "2001";

        assertEquals(2001, ExpiryDateUtil.yearFromString(y));
    }

    @Test
    public void yearIn4DigitFormat_isCorrect() throws Exception {
        int y = 0;

        assertEquals(2000, ExpiryDateUtil.yearInFourDigitFormat(y));

        y = 1;

        assertEquals(2001, ExpiryDateUtil.yearInFourDigitFormat(y));

        y = 19;

        assertEquals(2019, ExpiryDateUtil.yearInFourDigitFormat(y));

        y = 20;

        assertEquals(2020, ExpiryDateUtil.yearInFourDigitFormat(y));
    }

    @Test
    public void monthStringFromFormattedExpiryDate_isCorrect() throws Exception {
        String expiry = "01 / 22";
        String month = "01";
        String result = ExpiryDateUtil.monthStringFromExpiryString(expiry);

        assertEquals(true, result.equals(month));

        expiry = "12 / 33";
        month = "12";
        result = ExpiryDateUtil.monthStringFromExpiryString(expiry);

        assertEquals(true, result.equals(month));

        expiry = "08 / 33";
        month = "08";
        result = ExpiryDateUtil.monthStringFromExpiryString(expiry);

        assertEquals(true, result.equals(month));

        expiry = "10 / 00";
        month = "10";
        result = ExpiryDateUtil.monthStringFromExpiryString(expiry);

        assertEquals(true, result.equals(month));

        // check if Utils are format compliant
        expiry = "10 / 00";
        month = "1";
        result = ExpiryDateUtil.monthStringFromExpiryString(expiry);

        assertEquals(false, result.equals(month));
    }

    @Test
    public void yearStringFromFormattedExpiryDate_isCorrect() throws Exception {
        String expiry = "01 / 22";
        String year = "22";
        String result = ExpiryDateUtil.yearStringFromExpiryString(expiry);

        assertEquals(true, result.equals(year));

        expiry = "12 / 00";
        year = "00";
        result = ExpiryDateUtil.yearStringFromExpiryString(expiry);

        assertEquals(true, result.equals(year));

        expiry = "03 / 01";
        year = "01";
        result = ExpiryDateUtil.yearStringFromExpiryString(expiry);

        assertEquals(true, result.equals(year));

        expiry = "03 / 01";
        year = "1";
        result = ExpiryDateUtil.yearStringFromExpiryString(expiry);

        assertEquals(false, result.equals(year));
    }
}