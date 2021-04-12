package com.errorDefault.oc_19.data_request;

import android.annotation.SuppressLint;

import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;

public class CityDataRequestReader extends DataRequestReader {
    private static final Map<String, Integer> monthNumbers = new TreeMap<>();
    private static final Map<Integer, String> numberMonths = new TreeMap<>();

    private static int getMostRecentDateIndex(String data){
        LocalDate today = LocalDate.now();

        int index = data.indexOf(convertDateFormatDMY(today));
        while(index < 0){
            today = today.minusDays(1);
            index = data.indexOf(convertDateFormatDMY(today));
        }
        return index;
    }

    public static long getTotalCityCases(String data, String cityName) {
        data = data.substring(data.indexOf("\"DateSpecCollect\":\"Total\""));
        String search = String.format("\"%s\":", cityName.replace(' ', '_'));
        int totalIndex = data.indexOf(search) + search.length();
        return parseNumber(data, totalIndex);
    }


    public static long getDailyCityCases(String data, String cityName) {
        data = data.substring(getMostRecentDateIndex(data));
        String search = String.format("\"%s\":", cityName.replace(' ', '_'));
        int dailyIndex = data.indexOf(search) + search.length();
        return parseNumber(data, dailyIndex);
    }

    @SuppressLint("DefaultLocale")
    private static String convertDateFormatDMY(LocalDate date){
        return convertDateFormatDMY(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private static String convertDateFormatDMY(int day, int month, int year){
        if(numberMonths.isEmpty()){
            numberMonths.put(1, "Jan");
            numberMonths.put(2, "Feb");
            numberMonths.put(3, "Mar");
            numberMonths.put(4, "Apr");
            numberMonths.put(5, "May");
            numberMonths.put(6, "Jun");
            numberMonths.put(7, "Jul");
            numberMonths.put(8, "Aug");
            numberMonths.put(9, "Sep");
            numberMonths.put(10, "Oct");
            numberMonths.put(11, "Nov");
            numberMonths.put(12, "Dec");
        }
        return String.format("%d-%s-%d", day, numberMonths.get(month), year%100);
    }

    @SuppressLint("DefaultLocale")
    private static String convertDateFormatMDY(String date){
        if(monthNumbers.isEmpty()){
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
        int dateIndex = getMostRecentDateIndex(data);
        return convertDateFormatMDY(parseQuote(data, dateIndex));
    }
}
