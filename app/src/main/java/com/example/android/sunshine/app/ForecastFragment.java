/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MY_LOADER_ID = 0;
    private static final String CURR_POSITION = "current_position";

    private ListView mListView;
    private static int mCurrPosition;
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES

    };


    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_PRESSURE = 10;
    static final int COL_WIND = 11;
    static final int COL_DEGREES = 12;


    private ForecastAdapter mForecastAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {

    }


    //    tells the fragment that its activity has completed its own Activity.onCreate().
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    //pass in the URI to get weather starting from today, and create new CursorLoader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(ForecastFragment.class.getSimpleName(), "in onCreateLoader. Loader is created");
        //THIS IS THE CONTENT PROVIDER QUERY IN ONCREATEVIEW FUNCTION
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri baseUri;
        baseUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        // Sort order:  Ascending, by date: date ASC
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Log.d(ForecastFragment.class.getSimpleName(), baseUri.toString());

        return new CursorLoader(getActivity(), baseUri, FORECAST_COLUMNS, null, null, sortOrder);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        Log.d(ForecastFragment.class.getSimpleName(), "Finish loading");
        if (data == null) {
            Log.e(ForecastFragment.class.getSimpleName(), "cusros is null!!!");
        }
        mForecastAdapter.swapCursor(data);
        if(mCurrPosition!= ListView.INVALID_POSITION)
            mListView.smoothScrollToPosition(mCurrPosition);

    }


    //Task: INFLATE OPTIONS MENU in this class

    //do initial creation of the fragment.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //report that this fragment has option menus in order to handle menu events
        //Need this for onCreateOptionsMenu method below
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    /*
    When the user selects an item from the options menu (including action items in the app bar),
    the system calls your activity's onOptionsItemSelected() method.
    This method passes the MenuItem selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //get id of selected item
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        updateWeather(); //to get new weather data from openweather API into our database
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this); //restart loader
    }

    //helper method to get location preference value from sharedpreference
    private void updateWeather() {
        Log.e(ForecastFragment.class.getSimpleName(), "updateWeather");
        FetchWeatherTask task = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
//        Log.e(ForecastFragment.class.getSimpleName(), location);
        task.execute(location);
    }


    //override onStart, so updateWeather is called whenever the fragment starts
    // THIS FUNCTION CAN BE CALLED MULTIPLE TIMES (WHEN FRAGMENT STARTS)
//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.e(ForecastFragment.class.getSimpleName(), "updateWeather");
//        updateWeather();
//    }


    /*
    Called to have the fragment instantiate its user interface view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
//        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        //initially, adapter does not have cursor. Cursor will be swapped in when loader finished its data loading
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        //inflate the fragment_main layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
       mListView= (ListView) rootView.findViewById(R.id.listview_forcast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE));


                    //save selected position in savedInstancetate
                    mCurrPosition = position;

                    //call onItemSelected, for 1 pane start new activity, for 2 pane replace fragment
                    ((Callback) getActivity()).onItemSelected(dateUri);

                    //pass a URI for the data needed for the detail view.
//                    content://com.example.android.sunshine.app/weather/[location_query]/[date]
                    //this step is moved to onItemSelected function in MainActivity
//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                    startActivity(intent);
                }
            }
        });


        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(CURR_POSITION)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mCurrPosition = savedInstanceState.getInt(CURR_POSITION);
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCurrPosition != ListView.INVALID_POSITION)
            outState.putInt(CURR_POSITION, mCurrPosition);
        super.onSaveInstanceState(outState);
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */


}
