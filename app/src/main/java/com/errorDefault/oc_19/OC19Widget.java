package com.errorDefault.oc_19;

import com.errorDefault.oc_19.data_request.*;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class OC19Widget extends AppWidgetProvider {

    public static final String CITY_DAILY_CASES = "cdca", CITY_CUMUL_CASES = "ccca", CITY_DATE = "cda";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.oc19_widget);
            views.setOnClickPendingIntent(R.id.oc19_widget, pendingIntent);
            SharedPreferences widgetPrefs = context.getSharedPreferences(OC19ConfigActivity.SHARED_PREFS, MODE_PRIVATE);
            String selectedCity = widgetPrefs.getString(OC19ConfigActivity.SELECTED_CITY + appWidgetId, context.getResources().getString(R.string.defaultCityWidget));
            String cityDaily = widgetPrefs.getString(CITY_DAILY_CASES + appWidgetId, context.getResources().getString(R.string.defaultCount));
            String cityTotal = widgetPrefs.getString(CITY_CUMUL_CASES + appWidgetId, context.getResources().getString(R.string.defaultCount));
            String date = widgetPrefs.getString(CITY_DATE + appWidgetId, context.getResources().getString(R.string.defaultDateWidget));
            views.setTextViewText(R.id.oc19_widget_selectedCity, selectedCity);
            views.setTextViewText(R.id.oc19_widget_daily, cityDaily);
            views.setTextViewText(R.id.oc19_widget_total, cityTotal);
            views.setTextViewText(R.id.oc19_widget_date, date);
            appWidgetManager.updateAppWidget(appWidgetId, views);

            new Thread(new DataRequestRunnable(views, widgetPrefs, appWidgetId, appWidgetManager, context)).start();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), OC19Widget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds){
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences widgetPrefs = context.getSharedPreferences(OC19ConfigActivity.SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = widgetPrefs.edit();
            editor.remove(OC19ConfigActivity.SELECTED_CITY + appWidgetId);
            editor.remove(CITY_DAILY_CASES + appWidgetId);
            editor.remove(CITY_CUMUL_CASES + appWidgetId);
            editor.remove(CITY_DATE + appWidgetId);
            editor.apply();
        }
    }

    public static class DataRequestRunnable implements Runnable{
        RemoteViews views;
        SharedPreferences widgetPrefs;
        AppWidgetManager appWidgetManager;
        int appWidgetId;
        Context context;
        public DataRequestRunnable(RemoteViews views, SharedPreferences widgetPrefs, int appWidgetId,  AppWidgetManager appWidgetManager, Context context){
            this.views = views;
            this.widgetPrefs = widgetPrefs;
            this.appWidgetId = appWidgetId;
            this.appWidgetManager = appWidgetManager;
            this.context = context;
        }
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
                try{
                    String selectedCity = widgetPrefs.getString(OC19ConfigActivity.SELECTED_CITY + appWidgetId, context.getResources().getString(R.string.defaultCityWidget));
                    SharedPreferences.Editor editor = widgetPrefs.edit();
                    editor.putString(OC19ConfigActivity.SELECTED_CITY + appWidgetId, selectedCity);
                    editor.apply();
                    String cityData, cityDaily, cityTotal, date;
                    if(selectedCity.equals(context.getResources().getString(R.string.county))) {
                        cityData = new CountyDataRequest().requestData();
                        cityDaily = String.format("%d", CountyDataRequestReader.getDailyCountyCases(cityData));
                        cityTotal = String.format("%d", CountyDataRequestReader.getTotalCountyCases(cityData));
                        date = CountyDataRequestReader.getMostRecentDate(cityData);
                    } else if(selectedCity.equals(context.getResources().getString(R.string.defaultCityWidget))){
                        throw new IOException();
                    } else {
                        cityData = new CityDataRequest().requestData(selectedCity);
                        cityDaily = String.format("%d", CityDataRequestReader.getDailyCityCases(cityData, selectedCity));
                        cityTotal = String.format("%d", CityDataRequestReader.getTotalCityCases(cityData, selectedCity));
                        date = CityDataRequestReader.getMostRecentDate(cityData);
                    }

                    editor.putString(CITY_DAILY_CASES + appWidgetId, cityDaily);
                    editor.putString(CITY_CUMUL_CASES + appWidgetId, cityTotal);
                    editor.putString(CITY_DATE + appWidgetId, date);
                    editor.apply();

                    views.setTextViewText(R.id.oc19_widget_selectedCity, selectedCity);
                    views.setTextViewText(R.id.oc19_widget_daily, cityDaily);
                    views.setTextViewText(R.id.oc19_widget_total, cityTotal);
                    views.setTextViewText(R.id.oc19_widget_date, date);
                } catch(IOException ignored){

                } finally{
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
        }
    }
}