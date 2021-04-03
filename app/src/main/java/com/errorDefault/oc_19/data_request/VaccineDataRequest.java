package com.errorDefault.oc_19.data_request;

import java.net.MalformedURLException;
import java.net.URL;

public class VaccineDataRequest extends DataRequest {
    public static final String BASE_URL = "https://data.democratandchronicle.com/covid-19-vaccine-tracker/california/06/";
    @Override
    protected URL createURL(String... args) throws MalformedURLException {
        return new URL(BASE_URL);
    }
}
