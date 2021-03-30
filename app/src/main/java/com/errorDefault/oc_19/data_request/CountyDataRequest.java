package com.errorDefault.oc_19.data_request;

import java.net.MalformedURLException;
import java.net.URL;

public class CountyDataRequest extends DataRequest {
    private static final String BASE_URL = "https://occovid19.ochealthinfo.com/coronavirus-in-oc";
    protected URL createURL(String... args) throws MalformedURLException {
        return new URL(BASE_URL);
    }
}
