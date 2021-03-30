package com.errorDefault.oc_19.data_request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class DataRequest{
    private static final int CONNECT_TIMEOUT = 5000, READ_TIMEOUT = 5000;

    protected abstract URL createURL(String... args) throws MalformedURLException;

    protected HttpURLConnection connect(String... args) throws IOException {
        URL url = null;
        try {
            url = createURL(args);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        return connection;
    }

    public String requestData(String... args) throws IOException{
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        HttpURLConnection connection = null;
        try {
            connection = connect(args);

            int status = connection.getResponseCode();

            if(status > 299) {
                throw new IOException();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            while((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();
        } finally {
            connection.disconnect();
        }
        return responseContent.toString();
    }

}



