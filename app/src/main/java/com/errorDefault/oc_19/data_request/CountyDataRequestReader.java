package com.errorDefault.oc_19.data_request;


public class CountyDataRequestReader extends DataRequestReader {
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

    public static long getTotalCountyTests(String data){ return getCount(data, 5); }

    public static long getDailyCountyTests(String data){ return getCount(data, 6); }

    public static long getCurrentHospitalized(String data){ return getCount(data, 7); }

    public static long getCurrentICU(String data){ return getCount(data, 8); }

    public static String getMostRecentDate(String data){
        String search = "Posted Date: ";
        int startIndex = data.indexOf(search) + search.length();
        return parse(data, startIndex, '<');
    }
}
