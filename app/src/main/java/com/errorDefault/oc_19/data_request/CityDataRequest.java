package com.errorDefault.oc_19.data_request;

import java.net.MalformedURLException;
import java.net.URL;

public class CityDataRequest extends DataRequest {
    private static final String BASE_URL = "https://ochca.maps.arcgis.com/apps/opsdashboard/index.html#/5839a554eaac45c2b8e05dacc74c3bec";

    @Override
    protected URL createURL(String... args) throws MalformedURLException {
        return new URL(BASE_URL);
    }
}
