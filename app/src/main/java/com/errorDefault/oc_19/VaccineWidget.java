package com.errorDefault.oc_19;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.errorDefault.oc_19.data_request.VaccineDataRequest;
import com.errorDefault.oc_19.data_request.VaccineDataRequestReader;

import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class VaccineWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vaccine_widget);
            views.setOnClickPendingIntent(R.id.vaccine_widget, pendingIntent);

            SharedPreferences sharedPrefs = context.getSharedPreferences(OC19ConfigActivity.SHARED_PREFS, MODE_PRIVATE);
            if(sharedPrefs.contains(MainActivity.COUNTY_POPULATION)){
                String populationStr = sharedPrefs.getString(MainActivity.COUNTY_POPULATION, context.getResources().getString(R.string.defaultCount));
                String oneDoseStr = sharedPrefs.getString(MainActivity.COUNTY_ONE_DOSE, context.getResources().getString(R.string.defaultCount));
                String twoDosesStr = sharedPrefs.getString(MainActivity.COUNTY_TWO_DOSES, context.getResources().getString(R.string.defaultCount));
                String dateStr = sharedPrefs.getString(MainActivity.VACCINE_DATE, context.getResources().getString(R.string.defaultCount));

                long populationL = Long.parseLong(populationStr);
                long oneDoseL = Long.parseLong(oneDoseStr);
                long twoDosesL = Long.parseLong(twoDosesStr);

                views.setTextViewText(R.id.vaccine_widget_one, MainActivity.formatPercentage(oneDoseL, populationL));
                views.setTextViewText(R.id.vaccine_widget_two, MainActivity.formatPercentage(twoDosesL, populationL));
                views.setTextViewText(R.id.vaccine_widget_date, dateStr);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);

            new Thread(new VaccineWidget.VaccineDataRequestRunnable(views, sharedPrefs, appWidgetId, appWidgetManager, context)).start();
        }
    }

    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(MainActivity.VACCINE_UPDATE_ACTION)) {
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
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static class VaccineDataRequestRunnable implements Runnable{
        RemoteViews views;
        SharedPreferences widgetPrefs;
        AppWidgetManager appWidgetManager;
        int appWidgetId;
        Context context;

        public VaccineDataRequestRunnable(RemoteViews views, SharedPreferences widgetPrefs, int appWidgetId,  AppWidgetManager appWidgetManager, Context context){
            this.views = views;
            this.widgetPrefs = widgetPrefs;
            this.appWidgetId = appWidgetId;
            this.appWidgetManager = appWidgetManager;
            this.context = context;
        }

        @Override
        public void run() {
            try{
                String data = new VaccineDataRequest().requestData();

                long populationL = VaccineDataRequestReader.getPopulation(data);
                long oneDoseL = VaccineDataRequestReader.getOneDose(data);
                long twoDosesL = VaccineDataRequestReader.getTwoDoses(data);
                String dateStr = VaccineDataRequestReader.getMostRecentDate(data);

                views.setTextViewText(R.id.vaccine_widget_one, MainActivity.formatPercentage(oneDoseL, populationL));
                views.setTextViewText(R.id.vaccine_widget_two, MainActivity.formatPercentage(twoDosesL, populationL));
                views.setTextViewText(R.id.vaccine_widget_date, dateStr);

                SharedPreferences.Editor editor = widgetPrefs.edit();
                editor.putString(MainActivity.COUNTY_POPULATION, Long.valueOf(populationL).toString());
                editor.putString(MainActivity.COUNTY_ONE_DOSE, Long.valueOf(oneDoseL).toString());
                editor.putString(MainActivity.COUNTY_TWO_DOSES, Long.valueOf(twoDosesL).toString());
                editor.putString(MainActivity.VACCINE_DATE, dateStr);
                editor.apply();
            } catch(IOException ignored){

            } finally {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}