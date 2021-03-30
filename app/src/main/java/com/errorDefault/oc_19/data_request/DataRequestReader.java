package com.errorDefault.oc_19.data_request;

public abstract class DataRequestReader {
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

