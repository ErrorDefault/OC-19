package com.errorDefault.oc_19;

abstract class DataRequestReader {
    protected static long parseNumber(String data, int startIndex) {
        char digit = data.charAt(startIndex);
        StringBuilder numStr = new StringBuilder();
        while(( (48 <= digit && digit <= 57) || digit == ',') && startIndex < data.length()) {
            if(digit != ',')
                numStr.append(digit);
            digit = data.charAt(++startIndex);
        }
        return Long.parseLong(numStr.toString());
    }

    protected static String parse(String data, int startIndex, char terminator) {
        StringBuilder quoteStr = new StringBuilder();
        char c = data.charAt(startIndex);
        if(c == terminator) {
            c = data.charAt(++startIndex);
        }
        while(c != terminator && startIndex < data.length()) {
            quoteStr.append(c);
            c = data.charAt(++startIndex);
        }
        return quoteStr.toString();
    }

    protected static String parseQuote(String data, int startIndex) {
        return parse(data, startIndex, '"');
    }
}

class CountyDataRequestReader extends DataRequestReader {
    private static long getCount(String data, int instance) {
        String search = "\"casecount-panel-title\">";
        int startIndex = data.indexOf(search) + search.length();
        for(int i=1;i<instance;i++) {
            data = data.substring(startIndex);
            startIndex = data.indexOf(search) + search.length();
        }
        return parseNumber(data, startIndex);
    }

    public static long getTotalCountyCases(String data) {
        return getCount(data, 1);
    }

    public static long getDailyCountyCases(String data) {
        return getCount(data, 2);
    }

    public static long getTotalCountyDeaths(String data) {
        return getCount(data, 3);
    }

    public static long getDailyCountyDeaths(String data) {
        return getCount(data, 4);
    }

    public static long getTotalCountyRecovered(String data) {
        return getCount(data, 9);
    }

    public static String getMostRecentDate(String data){
        String search = "Posted Date: ";
        int startIndex = data.indexOf(search) + search.length();
        return parse(data, startIndex, '<');
    }
}

class CityDataRequestReader extends DataRequestReader {
    public static long getTotalCityCases(String data, String cityName) {
        String search = String.format("\"%s\":", cityName.replace(' ', '_'));
        int totalIndex = data.lastIndexOf(search) + search.length();
        return parseNumber(data, totalIndex);
    }

    public static long getDailyCityCases(String data, String cityName) {
        String search = String.format("\"%s\":", cityName.replace(' ', '_'));
        int endIndex = data.lastIndexOf(search);
        data = data.substring(0, endIndex);
        int totalIndex = data.lastIndexOf(search) + search.length();
        return parseNumber(data, totalIndex);
    }

    public static String getMostRecentDate(String data) {
        String search = "\"DateSpecCollect\":";
        int endIndex = data.lastIndexOf(search);
        data = data.substring(0, endIndex);
        int dateIndex = data.lastIndexOf(search) + search.length();
        return parseQuote(data, dateIndex);
    }
}
