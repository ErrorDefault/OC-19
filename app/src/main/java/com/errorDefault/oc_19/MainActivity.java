package com.errorDefault.oc_19;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

abstract class DataRequest{
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

class CountyDataRequest extends DataRequest {
    private static final String BASE_URL = "https://opendata.arcgis.com/datasets/e5ceebe7edba44cc8f875ca54cc2341a_0/FeatureServer/0/query?where=1%3D1&outFields=date,cumulativecases_specimen,dailycases_specimen,cumulativedeaths_date,dailydeaths_date";
    protected URL createURL(String... args) throws MalformedURLException {
        return new URL(BASE_URL);
    }
}

class CountyDataRequest2 extends DataRequest {
    private static final String BASE_URL = "https://occovid19.ochealthinfo.com/coronavirus-in-oc";
    protected URL createURL(String... args) throws MalformedURLException {
        return new URL(BASE_URL);
    }
}

class CityDataRequest extends DataRequest {
    private static final String BASE_URL = "https://opendata.arcgis.com/datasets/772f5cdbb99c4f6689ed1460c26f4b05_0/FeatureServer/0/query?";

    protected URL createURL(String ... args) throws MalformedURLException {
        StringBuilder urlStr = new StringBuilder(BASE_URL);
        urlStr.append("outFields=");
        urlStr.append(args[0].replace(' ', '_'));
        urlStr.append(",DateSpecCollect,Total");
        System.out.println(urlStr);
        return new URL(urlStr.toString());
    }
}

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
    public static final String NULL = "null";

    private static int getLastNumberValueIndex(String data, String search){
        int endIndex = data.lastIndexOf(search);
        while(data.substring(endIndex + search.length(), endIndex + search.length() + NULL.length()).equals(NULL)){
            data = data.substring(0, endIndex);
            endIndex = data.lastIndexOf(search);
        }
        return endIndex;
    }

    private static long getLastNumberValue(String data, String search){
        int endIndex = getLastNumberValueIndex(data, search);
        return parseNumber(data, endIndex + search.length());
    }

    public static long getTotalCountyCases(String data) {
        return getLastNumberValue(data, "\"cumulativecases_specimen\":");
    }

    public static long getDailyCountyCases(String data) {
        return getLastNumberValue(data, "\"dailycases_specimen\":");
    }

    public static long getTotalCountyDeaths(String data) {
        return getLastNumberValue(data, "\"cumulativedeaths_date\":");
    }

    public static long getDailyCountyDeaths(String data) {
        return getLastNumberValue(data, "\"dailydeaths_date\":");
    }

    public static String getMostRecentDateCases(String data){
        String search = "\"date\":";
        int endIndex = getLastNumberValueIndex(data, "\"cumulativecases_specimen\":");
        data = data.substring(0, endIndex);
        int dateIndex = data.lastIndexOf(search) + search.length();
        return parseQuote(data, dateIndex);

    }

    public static String getMostRecentDateDeaths(String data){
        String search = "\"date\":";
        int endIndex = getLastNumberValueIndex(data, "\"cumulativedeaths_date\":");
        data = data.substring(0, endIndex);
        int dateIndex = data.lastIndexOf(search) + search.length();
        return parseQuote(data, dateIndex);
    }
}

class CountyDataRequestReader2 extends DataRequestReader {
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

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs", CITY = "city", CITY_DAILY_CASES = "cidc", CITY_CUMUL_CASES = "cicc",
            COUNTY_DAILY_CASES = "codc", COUNTY_CUMUL_CASES = "cocc", COUNTY_DAILY_DEATHS = "codd", COUNTY_TOTAL_DEATHS = "cotd",
            COUNTY_CUMUL_RECOVERED = "cocr", COUNTY_DATE = "coda", CITY_DATE = "cida";

    private String mostRecentCityCasesDateStr = null;

    private TextView countyDailyCases, countyCumulativeCases, countyDailyDeaths, countyTotalDeaths, countyCumulativeRecovered, cityDailyCases, cityCumulativeCases, city, mostRecentCountyDate, mostRecentCityDate;

    private Spinner citySpinner;

    private Handler mainHandler = new Handler();

    private Map<String, Long[]> cache = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        // Initialize Text
        countyDailyCases = findViewById(R.id.countyDailyCases);
        countyCumulativeCases = findViewById(R.id.countyCumulativeCases);
        countyDailyDeaths = findViewById(R.id.countyDailyDeaths);
        countyTotalDeaths = findViewById(R.id.countyCumulativeDeaths);
        countyCumulativeRecovered = findViewById(R.id.countyCumulativeRecovered);
        cityDailyCases = findViewById(R.id.cityDailyCases);
        cityCumulativeCases = findViewById(R.id.cityCumulativeCases);
        city = findViewById(R.id.city);
        mostRecentCityDate = findViewById(R.id.cityDate);
        mostRecentCountyDate = findViewById(R.id.countyDate);
        // Initialize City Selector Spinner
        citySpinner = findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> citySpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.city_list, android.R.layout.simple_spinner_item);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(citySpinnerAdapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new Thread(new CityDataRequestRunnable()).start();
            }

                @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        loadData();
        new Thread(new CountyDataRequestRunnable()).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                cache = new TreeMap<>();
                new Thread(new CountyDataRequestRunnable()).start();
                new Thread(new CityDataRequestRunnable()).start();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CITY, city.getText().toString());
        editor.putString(CITY_DAILY_CASES, cityDailyCases.getText().toString());
        editor.putString(CITY_CUMUL_CASES, cityCumulativeCases.getText().toString());
        editor.putString(COUNTY_DAILY_CASES, countyDailyCases.getText().toString());
        editor.putString(COUNTY_CUMUL_CASES, countyCumulativeCases.getText().toString());
        editor.putString(COUNTY_DAILY_DEATHS, countyDailyDeaths.getText().toString());
        editor.putString(COUNTY_TOTAL_DEATHS, countyTotalDeaths.getText().toString());
        editor.putString(COUNTY_CUMUL_RECOVERED, countyCumulativeRecovered.getText().toString());
        editor.putString(COUNTY_DATE, mostRecentCountyDate.getText().toString());
        editor.putString(CITY_DATE, mostRecentCityDate.getText().toString());
        editor.apply();
    }

    private int getIndexByValue(Spinner spinner, String value){
        for(int i=0;i<spinner.getCount();i++){
            if(spinner.getItemAtPosition(i).toString().equals(value))
                return i;
        }
        return 0;
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        city.setText(sharedPreferences.getString(CITY, citySpinner.getItemAtPosition(0).toString()));
        cityDailyCases.setText(sharedPreferences.getString(CITY_DAILY_CASES, "0"));
        cityCumulativeCases.setText(sharedPreferences.getString(CITY_CUMUL_CASES, "0"));
        countyDailyCases.setText(sharedPreferences.getString(COUNTY_DAILY_CASES, "0"));
        countyCumulativeCases.setText(sharedPreferences.getString(COUNTY_CUMUL_CASES, "0"));
        countyDailyDeaths.setText(sharedPreferences.getString(COUNTY_DAILY_DEATHS, "0"));
        countyTotalDeaths.setText(sharedPreferences.getString(COUNTY_TOTAL_DEATHS, "0"));
        countyCumulativeRecovered.setText(sharedPreferences.getString(COUNTY_CUMUL_RECOVERED, "0"));
        mostRecentCountyDate.setText(sharedPreferences.getString(COUNTY_DATE, "as of"));
        mostRecentCityDate.setText(sharedPreferences.getString(CITY_DATE, "as of"));
        citySpinner.setSelection(getIndexByValue(citySpinner, city.getText().toString()));
    }

    class CountyDataRequestRunnable implements Runnable{

        @Override
        public void run() {
            try {
                String data = new CountyDataRequest2().requestData();
                mainHandler.post(new Runnable() {
                    public void run() {
                        countyDailyCases.setText(String.format("%d", CountyDataRequestReader2.getDailyCountyCases(data)));
                        countyCumulativeCases.setText(String.format("%d", CountyDataRequestReader2.getTotalCountyCases(data)));
                        countyDailyDeaths.setText(String.format("%d", CountyDataRequestReader2.getDailyCountyDeaths(data)));
                        countyTotalDeaths.setText(String.format("%d", CountyDataRequestReader2.getTotalCountyDeaths(data)));
                        countyCumulativeRecovered.setText(String.format("%d", CountyDataRequestReader2.getTotalCountyRecovered(data)));
                        mostRecentCountyDate.setText(String.format("as of %s", CountyDataRequestReader2.getMostRecentDate(data)));
                    }
                });
            } catch (IOException e) {
                mainHandler.post(new Runnable(){

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Unable to retrieve county data.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    class CityDataRequestRunnable implements Runnable{
        private Map<String, Integer> monthNumbers = new TreeMap<>();
        private boolean initialized = false;
        @Override
        public void run() {
            String selectedCity = citySpinner.getItemAtPosition(citySpinner.getSelectedItemPosition()).toString();
            try {
                if (!cache.containsKey(selectedCity)) {
                    String data = new CityDataRequest().requestData(selectedCity);
                    Long[] cityData = {CityDataRequestReader.getDailyCityCases(data, selectedCity), CityDataRequestReader.getTotalCityCases(data, selectedCity)};
                    cache.put(selectedCity, cityData);
                    if (mostRecentCityCasesDateStr == null)
                        mostRecentCityCasesDateStr = convertDateFormat(CityDataRequestReader.getMostRecentDate(data));
                }
                mainHandler.post(new Runnable() {
                    public void run() {
                        cityDailyCases.setText(cache.get(selectedCity)[0].toString());
                        cityCumulativeCases.setText(cache.get(selectedCity)[1].toString());
                        mostRecentCityDate.setText(String.format("as of %s", mostRecentCityCasesDateStr));
                        city.setText(selectedCity);
                    }
                });
            } catch (IOException e){
                mainHandler.post(new Runnable(){

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Unable to retrieve city data.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        private String convertDateFormat(String date){
            if(!initialized){
                initialized = true;
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
    }
}