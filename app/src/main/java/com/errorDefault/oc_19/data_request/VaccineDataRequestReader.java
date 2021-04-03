package com.errorDefault.oc_19.data_request;

import java.util.Map;
import java.util.TreeMap;

public class VaccineDataRequestReader extends DataRequestReader {
    private static final String ORANGE_COUNTY = "Orange County";
    private static final Map<String, Integer> monthNumbers = new TreeMap<>();
    private static boolean initialized = false;

    private static String convertDateFormat(String date){
        if(!initialized){
            initialized = true;
            monthNumbers.put("January", 1);
            monthNumbers.put("February", 2);
            monthNumbers.put("March", 3);
            monthNumbers.put("April", 4);
            monthNumbers.put("May", 5);
            monthNumbers.put("June", 6);
            monthNumbers.put("July", 7);
            monthNumbers.put("August", 8);
            monthNumbers.put("September", 9);
            monthNumbers.put("October", 10);
            monthNumbers.put("November", 11);
            monthNumbers.put("December", 12);
        }
        String month = date.substring(0, date.indexOf(' '));
        String day = date.substring(date.indexOf(' ')+1, date.lastIndexOf(','));
        String year = date.substring(date.lastIndexOf(' ')+1);
        return String.format("%d/%s/%s", monthNumbers.get(month), day, year);
    }

    public static String getMostRecentDate(String data){
        String search = "As of ";
        int startIndex = data.indexOf(search) + search.length();
        return convertDateFormat(parse(data, startIndex, '<'));
    }

    private static long getOCNumericData(String data, String search){
        data = data.substring(data.indexOf(ORANGE_COUNTY));
        int startIndex = data.indexOf(search) + search.length();
        return parseNumber(data, startIndex);
    }

    public static long getPopulation(String data){
        return getOCNumericData(data, "\"tbvar covd\">");
    }

    public static long getOneDose(String data){
        return getOCNumericData(data, "\"tbvar cove\">");
    }

    public static long getTwoDoses(String data){
        return getOCNumericData(data, "\"tbvar covr\">");
    }
}
