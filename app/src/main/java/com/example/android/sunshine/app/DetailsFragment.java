package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by anhqu on 10/14/2016.
 */

public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private String forecastString;

//    private static final String[] DETAIL_COLUMNS = {
//            // In this case the id needs to be fully qualified with a table name, since
//            // the content provider joins the location & weather tables in the background
//            // (both have an _id column)
//            // On the one hand, that's annoying.  On the other, you can search the weather table
//            // using the location set by the user, which is only in the Location table.
//            // So the convenience is worth it.
//            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
//            WeatherContract.WeatherEntry.COLUMN_DATE,
//            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
//            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
//            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
//            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
//            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
//            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
//            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
//            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
//            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
//            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
//            WeatherContract.WeatherEntry.COLUMN_DEGREES
//    };


    private static final int DETAIL_LOADER_ID = 1;


    public static class DetailViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView todayView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidView;
        public final TextView pressureView;
        public final TextView windView;


        public DetailViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            todayView = (TextView) view.findViewById(R.id.detail_today_textview);

            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            humidView = (TextView) view.findViewById(R.id.detail_humidity);
            pressureView = (TextView)view.findViewById(R.id.detail_pressure);
            windView = (TextView)view.findViewById(R.id.detail_wind);

        }
    }

    private DetailViewHolder viewHolder;
    public DetailsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //report that this fragment has option menus in order to handle menu events
        //Need this for onCreateOptionsMenu method below
        setHasOptionsMenu(true);
    }

    private Intent createSharedForecastIntent()
    {
        Intent sharedIntent = new Intent(Intent.ACTION_SEND);
        //prevents activity we're sharing to from BEING PLACED ONTO THE ACTIVITY STACK, otherwise
        //we will end up in another application (that handles the shared intent) when we want to return to the original app
        sharedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharedIntent.setType("text/plain");
        sharedIntent.putExtra(Intent.EXTRA_TEXT, forecastString + " #Sunshine");
        return sharedIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//            Log.d(LOG_TAG, "in onCreateOptionMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.share, menu);
        // Locate MenuItem with ShareActionProvider and retrieve the share menu item
        MenuItem item=menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        //set share intent
        if(mShareActionProvider!=null && forecastString != null)
        {
            mShareActionProvider.setShareIntent(createSharedForecastIntent());
        }

        //share intent is set in onLoadFinished as well since we dont know which one will occur first?

        else{
            Log.e(DetailActivity.class.getSimpleName(),"Share Action Provider is null or Forecast string is null");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID,null,this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//            Log.d(LOG_TAG, "in onCreateView");

        //inflate the detail view
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        //intent extraction will be taken care in onCreateLoader in LoaderManager
        //because data might be loaded before onCreateView?

        viewHolder = new DetailViewHolder(rootView);
        //view setting will be taken care in onLoadFinished

        return rootView;
    }


    //Task: implement details view using cursorloader, loading data URI from intent

    //get URI from content and create new cursorloader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//            Log.d(LOG_TAG, "in onCreateLoader in LoaderManager");
        Intent intent = getActivity().getIntent();
        if(intent!=null) {
//                forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
//                textView.setText(intent.getStringExtra(Intent.EXTRA_TEXT));


            Uri temp = intent.getData(); //get that URI
//                content://com.example.android.sunshine.app/weather/[location_query]/[date]

            return new CursorLoader(getActivity(), temp,ForecastFragment.FORECAST_COLUMNS,null,null,null);
        }
        return null;

    }


    //get data from cursor, get text view and set the text
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//            Log.d(LOG_TAG, "in onLoadFinished in LoaderManager. Cursor position: " + data.getPosition());
        if (!data.moveToFirst()) { return; }
        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(),data.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),isMetric);
        String low = Utility.formatTemperature(getActivity(),data.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP),isMetric);
        String date = Utility.getFormattedMonthDay(getActivity(),data.getLong(ForecastFragment.COL_WEATHER_DATE));
        String desc = data.getString(ForecastFragment.COL_WEATHER_DESC);

        if(getView()!=null)
        {
            int weatherId = data.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
            viewHolder.todayView.setText(Utility.getDayName(getActivity(),data.getLong(ForecastFragment.COL_WEATHER_DATE)));
            viewHolder.dateView.setText(date);
            viewHolder.highTempView.setText(high);
            viewHolder.lowTempView.setText(low);

            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            viewHolder.descriptionView.setText(desc);

            viewHolder.humidView.setText(String.format(getActivity().getString(R.string.format_humidity),
                    data.getDouble(ForecastFragment.COL_HUMIDITY)
                    ));
            viewHolder.pressureView.setText(String.format(getActivity().getString(R.string.format_pressure),
                    data.getDouble(ForecastFragment.COL_PRESSURE)
                    ));

            viewHolder.windView.setText(Utility.getFormattedWind(getActivity(),data.getFloat(ForecastFragment.COL_WIND), data.getFloat(ForecastFragment.COL_DEGREES)));
        }

        forecastString = String.format("%s - %s - %s/%s",date,desc,high,low);


        //WE ARE ASSUMING DETAIL VIEW EXISTS ALREADY (by ONCREATEVIEW function)
//            if(getView()!= null) {
//                TextView textView = (TextView) getView().findViewById(R.id.detailText);
//                textView.setText(forecastString);
//            }






        //set share intent when forecastString is created
        if(mShareActionProvider!=null)
        {
            mShareActionProvider.setShareIntent(createSharedForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
