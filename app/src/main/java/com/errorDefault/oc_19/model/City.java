package com.errorDefault.oc_19.model;

import android.content.Context;

import java.util.ArrayList;

public class City {
    private final String cityName;

    public City(String cityName){
        this.cityName = cityName;
    }

    public String getCityName(){
        return cityName;
    }

    public static ArrayList<City> createCityList(Context context, int id){
        ArrayList<City> cityList = new ArrayList<>();
        String[] cRaces = context.getResources().getStringArray(id);
        for (String cRace : cRaces) cityList.add(new City(cRace));
        return cityList;
    }
}
