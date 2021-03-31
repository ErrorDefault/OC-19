package com.errorDefault.oc_19.data_request;

import android.annotation.SuppressLint;

import java.util.Map;
import java.util.TreeMap;

public class CityDataRequestReader extends DataRequestReader {
    private static final Map<String, Integer> monthNumbers = new TreeMap<>();
    private static boolean initialized = false;

    public static long getTotalCityCases(String data, String cityName) {
        int startIndex = data.indexOf(cityName);
        System.out.println("City Index: " + startIndex);
        data = data.substring(startIndex);
        String search = "-&nbsp;";
        int totalIndex = data.lastIndexOf(search) + search.length();
        System.out.println("Total Index: " + totalIndex);
        return 0;
    }

    public static long getDailyCityCases(String data, String cityName) {
        /*String search = String.format("\"%s\":", cityName.replace(' ', '_'));
        int endIndex = data.lastIndexOf(search);
        data = data.substring(0, endIndex);
        int totalIndex = data.lastIndexOf(search) + search.length();
        return parseNumber(data, totalIndex);*/
        return 0;
    }

    @SuppressLint("DefaultLocale")
    private static String convertDateFormat(String date){
        if(!initialized){
            initialized = true;
            monthNumbers.put("Jan", 1);
            monthNumbers.put("Feb", 2);
            monthNumbers.put("Mar", 3);
            monthNumbers.put("Apr", 4);
            monthNumbers.put("May", 5);
            monthNumbers.put("Jun", 6);
            monthNumbers.put("Jul", 7);
            monthNumbers.put("Aug", 8);
            monthNumbers.put("Sep", 9);
            monthNumbers.put("Oct", 10);
            monthNumbers.put("Nov", 11);
            monthNumbers.put("Dec", 12);
        }
        String day = date.substring(0, date.indexOf('-'));
        String month = date.substring(date.indexOf('-')+1, date.lastIndexOf('-'));
        String year = "20" + date.substring(date.lastIndexOf('-')+1);
        return String.format("%d/%s/%s", monthNumbers.get(month), day, year);
    }

    public static String getMostRecentDate(String data) {
        String search = "Updated: ";
        int startIndex = data.indexOf(search) + search.length();
        return parse(data, startIndex, '<');
    }
}
