package com.errorDefault.oc_19;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.errorDefault.oc_19.data.OC19ConfigAdapter;
import com.errorDefault.oc_19.model.City;

import java.util.ArrayList;

public class OC19ConfigActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "widget_prefs";
    public static final String SELECTED_CITY = "widget_city";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private ArrayList<City> cityList;
    private City selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oc19_config);

        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if(extras != null){
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        if(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
        }

        RecyclerView recyclerView = findViewById(R.id.oc19_config_cityList);
        cityList = City.createCityList(this, R.array.city_list);
        cityList.add(0, new City(getResources().getString(R.string.county)));
        OC19ConfigAdapter adapter = new OC19ConfigAdapter(cityList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        adapter.setOnItemClickListener(position -> {
            selectedCity = cityList.get(position);
            final Context context = OC19ConfigActivity.this;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.oc19_widget);
            views.setOnClickPendingIntent(R.id.oc19_widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(SELECTED_CITY + appWidgetId, selectedCity.getCityName());
            editor.apply();
            new Thread(new OC19Widget.DataRequestRunnable(views, prefs, appWidgetId, appWidgetManager, getResources().getString(R.string.county))).start();

            Intent resultValue1 = new Intent();
            resultValue1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue1);
            finish();
        });
    }
}