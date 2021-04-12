package com.errorDefault.oc_19.data_request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

public class CityDataRequest extends DataRequest {
    private static final String BASE_URL = "https://services2.arcgis.com/LORzk2hk9xzHouw9/arcgis/rest/services/Public_OC_COVID_Cases_by_City_by_Day/FeatureServer/0/query?where=1%3D1&";

    protected URL createURL(String ... args) throws MalformedURLException {
        StringBuilder urlStr = new StringBuilder(BASE_URL);
        urlStr.append("outFields=");
        urlStr.append("DateSpecCollect,");
        urlStr.append(args[0].replace(' ', '_'));
        urlStr.append(",Total");
        urlStr.append("&outSR=4326&f=json");
        return new URL(urlStr.toString());
    }
}
