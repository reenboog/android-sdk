package com.fidel.sdk;

/**
 * Created by reenboog on 7/24/17.
 */

public class CountriesUtil {
    private static String[] countries = {"United Kingdom", "Ireland"};
    private static String[] codes = {"GBR", "IRL"};

    public static String[] countries() {
        return countries;
    }
    public static String[] codes() {return codes;}

    public static String countryByIndex(int i) {
        if(i < 0 || i >= countries.length) {
            return null;
        } else {
            return countries[i];
        }
    }

    public static int indexForCountry(String country) {
        for(int i = 0; i < countries.length; ++i) {
            String ctr = countries[i];

            if(ctr.equals(country)) {
                return i;
            }
        }

        return -1;
    }

    public static String countryCodeForCountry(String country) {
        for(int i = 0; i < countries.length; ++i) {
            String ctr = countries[i];
            if(ctr.equals(country)) {
                return codes[i];
            }
        }

        return null;
    }

    public static String countryForCountryCode(String code) {
        for(int i = 0; i < codes.length; ++i) {
            String cd = codes[i];
            if(cd.equals(code)) {
                return countries[i];
            }
        }

        return null;
    }

}
