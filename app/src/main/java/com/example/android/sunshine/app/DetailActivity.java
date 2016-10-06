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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailsFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);


        return true;
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id ==R.id.action_settings)
        {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            Toast.makeText(this,"called from detail setting",Toast.LENGTH_SHORT).show();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
        private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
        private ShareActionProvider mShareActionProvider;
        private String forecastString;

        private static final String[] DETAIL_COLUMNS = {
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
                WeatherContract.LocationEntry.COLUMN_COORD_LONG



        };
        private static final int DETAIL_LOADER_ID = 1;
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

                return new CursorLoader(getActivity(), temp,DETAIL_COLUMNS,null,null,null);
            }
            return null;

        }


        //get data from cursor, get text view and set the text
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//            Log.d(LOG_TAG, "in onLoadFinished in LoaderManager. Cursor position: " + data.getPosition());
            if (!data.moveToFirst()) { return; }
            boolean isMetric = Utility.isMetric(getActivity());
            String temperature = Utility.formatTemperature(data.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP),isMetric)
                    + "/"+
                    Utility.formatTemperature(data.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),isMetric);
            String desc = data.getString(ForecastFragment.COL_WEATHER_DESC);
            String date = Utility.formatDate(data.getLong(ForecastFragment.COL_WEATHER_DATE));

            forecastString = date + "-" + desc + "-" + temperature;

            //WE ARE ASSUMING DETAIL VIEW EXISTS ALREADY (by ONCREATEVIEW function)
            if(getView()!= null) {
                TextView textView = (TextView) getView().findViewById(R.id.detailText);
                textView.setText(forecastString);
            }

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
}