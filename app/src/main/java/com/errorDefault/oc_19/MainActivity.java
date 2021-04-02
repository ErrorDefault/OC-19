package com.errorDefault.oc_19;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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


import com.errorDefault.oc_19.data_request.CityDataRequest;
import com.errorDefault.oc_19.data_request.CityDataRequestReader;
import com.errorDefault.oc_19.data_request.CountyDataRequest;
import com.errorDefault.oc_19.data_request.CountyDataRequestReader;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs", CITY = "city", CITY_DAILY_CASES = "cidc", CITY_CUMUL_CASES = "cicc",
            COUNTY_DAILY_CASES = "codc", COUNTY_CUMUL_CASES = "cocc", COUNTY_DAILY_DEATHS = "codd", COUNTY_TOTAL_DEATHS = "cotd",
            COUNTY_CUMUL_RECOVERED = "cocr", COUNTY_DAILY_TESTS = "codt", COUNTY_CUMUL_TESTS = "coct", COUNTY_ICU = "cicu",
            COUNTY_HOSPITALIZED = "coh", COUNTY_DATE = "coda", CITY_DATE = "cida";

    private String mostRecentCityCasesDateStr = null;

    private TextView cityDailyCases, cityCumulativeCases, city,
            countyDailyCases, countyCumulativeCases, countyDailyDeaths, countyTotalDeaths,
            countyDailyTests, countyCumulativeTests, countyICU, countyHospitalized,
            countyCumulativeRecovered, mostRecentCountyDate, mostRecentCityDate;

    private Spinner citySpinner;

    private final Handler mainHandler = new Handler();

    private Map<String, Long[]> cache = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        // Initialize TextViews
        cityDailyCases = findViewById(R.id.cityDailyCases);
        cityCumulativeCases = findViewById(R.id.cityCumulativeCases);
        city = findViewById(R.id.city);
        countyDailyCases = findViewById(R.id.countyDailyCases);
        countyCumulativeCases = findViewById(R.id.countyCumulativeCases);
        countyDailyDeaths = findViewById(R.id.countyDailyDeaths);
        countyTotalDeaths = findViewById(R.id.countyCumulativeDeaths);
        countyDailyTests = findViewById(R.id.countyDailyTests);
        countyCumulativeTests = findViewById(R.id.countyCumulativeTests);
        countyICU = findViewById(R.id.countyICU);
        countyHospitalized = findViewById(R.id.countyHospitalized);
        countyCumulativeRecovered = findViewById(R.id.countyCumulativeRecovered);
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
        if (item.getItemId() == R.id.refresh) {
            cache = new TreeMap<>();
            new Thread(new CountyDataRequestRunnable()).start();
            new Thread(new CityDataRequestRunnable()).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        editor.putString(COUNTY_DAILY_TESTS, countyDailyTests.getText().toString());
        editor.putString(COUNTY_CUMUL_TESTS, countyCumulativeTests.getText().toString());
        editor.putString(COUNTY_ICU, countyICU.getText().toString());
        editor.putString(COUNTY_HOSPITALIZED, countyHospitalized.getText().toString());
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
        countyDailyTests.setText(sharedPreferences.getString(COUNTY_DAILY_CASES, "0"));
        countyCumulativeTests.setText(sharedPreferences.getString(COUNTY_CUMUL_TESTS, "0"));
        countyICU.setText(sharedPreferences.getString(COUNTY_ICU, "0"));
        countyHospitalized.setText(sharedPreferences.getString(COUNTY_HOSPITALIZED, "0"));
        countyCumulativeRecovered.setText(sharedPreferences.getString(COUNTY_CUMUL_RECOVERED, "0"));

        mostRecentCountyDate.setText(sharedPreferences.getString(COUNTY_DATE, "as of"));
        mostRecentCityDate.setText(sharedPreferences.getString(CITY_DATE, "as of"));

        citySpinner.setSelection(getIndexByValue(citySpinner, city.getText().toString()));
    }

    class CountyDataRequestRunnable implements Runnable{

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            try {
                String data = new CountyDataRequest().requestData();
                mainHandler.post(() -> {
                    countyDailyCases.setText(String.format("%d", CountyDataRequestReader.getDailyCountyCases(data)));
                    countyCumulativeCases.setText(String.format("%d", CountyDataRequestReader.getTotalCountyCases(data)));

                    countyDailyDeaths.setText(String.format("%d", CountyDataRequestReader.getDailyCountyDeaths(data)));
                    countyTotalDeaths.setText(String.format("%d", CountyDataRequestReader.getTotalCountyDeaths(data)));

                    countyDailyTests.setText(String.format("%d", CountyDataRequestReader.getDailyCountyTests(data)));
                    countyCumulativeTests.setText(String.format("%d", CountyDataRequestReader.getTotalCountyTests(data)));

                    countyICU.setText(String.format("%d", CountyDataRequestReader.getCurrentICU(data)));
                    countyHospitalized.setText(String.format("%d", CountyDataRequestReader.getCurrentHospitalized(data)));

                    countyCumulativeRecovered.setText(String.format("%d", CountyDataRequestReader.getTotalCountyRecovered(data)));
                    mostRecentCountyDate.setText(String.format("as of %s", CountyDataRequestReader.getMostRecentDate(data)));
                });
            } catch (IOException e) {
                mainHandler.post(() -> Toast.makeText(getApplicationContext(), "Unable to retrieve county data.", Toast.LENGTH_LONG).show());
            }
        }
    }

    class CityDataRequestRunnable implements Runnable{
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            String selectedCity = citySpinner.getItemAtPosition(citySpinner.getSelectedItemPosition()).toString();
            try {
                if (!cache.containsKey(selectedCity)) {
                    String data = new CityDataRequest().requestData(selectedCity);
                    Long[] cityData = {CityDataRequestReader.getDailyCityCases(data, selectedCity), CityDataRequestReader.getTotalCityCases(data, selectedCity)};
                    cache.put(selectedCity, cityData);
                    if (mostRecentCityCasesDateStr == null)
                        mostRecentCityCasesDateStr = CityDataRequestReader.getMostRecentDate(data);
                }
                mainHandler.post(() -> {
                    cityDailyCases.setText(String.format("%d", cache.get(selectedCity)[0]));
                    cityCumulativeCases.setText(String.format("%d", cache.get(selectedCity)[1]));
                    mostRecentCityDate.setText(String.format("as of %s", mostRecentCityCasesDateStr));
                    city.setText(selectedCity);
                });
            } catch (IOException e){
                mainHandler.post(() -> Toast.makeText(getApplicationContext(), "Unable to retrieve city data.", Toast.LENGTH_LONG).show());
            }
        }
    }
}