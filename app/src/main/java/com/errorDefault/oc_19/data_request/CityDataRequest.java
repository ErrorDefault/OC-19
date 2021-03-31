package com.errorDefault.oc_19.data_request;

import java.net.MalformedURLException;
import java.net.URL;

public class CityDataRequest extends DataRequest {
    private static final String BASE_URL = "https://opendata.arcgis.com/datasets/772f5cdbb99c4f6689ed1460c26f4b05_0/FeatureServer/0/query?";

    protected URL createURL(String ... args) throws MalformedURLException {
        StringBuilder urlStr = new StringBuilder(BASE_URL);
        urlStr.append("outFields=");
        urlStr.append(args[0].replace(' ', '_'));
        urlStr.append(",DateSpecCollect,Total");
        return new URL(urlStr.toString());
    }
}
