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
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MY_LOADER_ID = 0;

    private static final String[] FORECAST_COLUMNS = {
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




    private ForecastAdapter mForecastAdapter;


    public ForecastFragment() {

    }


//    tells the fragment that its activity has completed its own Activity.onCreate().
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID,null,this);
        super.onActivityCreated(savedInstanceState);

    }

    //pass in the URI to get weather starting from today, and create new CursorLoader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Log.d(ForecastFragment.class.getSimpleName(), "in onCreateLoader. Loader is created");
        //THIS IS THE CONTENT PROVIDER QUERY IN ONCREATEVIEW FUNCTION
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri baseUri;
        baseUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        // Sort order:  Ascending, by date: date ASC
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(),baseUri,FORECAST_COLUMNS,null,null,sortOrder);

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

        mForecastAdapter.swapCursor(data);
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
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forcast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    //pass a URI for the data needed for the detail view.
//                    content://com.example.android.sunshine.app/weather/[location_query]/[date]
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */


//    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        @Override
//        protected String[] doInBackground(String... params) {
//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//
//            if(params.length==0) return null;
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            String format = "json";
//            String units = "metric";
//            int numDays = 7;
//
//            final String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are avaiable at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//
//                //Use URI builder to build the URI with custom key + parameter
//                //to build http://api.openweathermap.org/data/2.5/forecast/daily?q=77017,us&mode=json&units=metric&cnt=7&APPID=YOURKEYHERE
//                Uri builtUri = Uri.parse(baseURL).buildUpon()
//                        .appendQueryParameter("q",params[0])
//                        .appendQueryParameter("mode",format)
//                        .appendQueryParameter("units",params[1])
//                        .appendQueryParameter("cnt",Integer.toString(numDays))
//                        .appendQueryParameter("APPID",BuildConfig.OPEN_WEATHER_MAP_API_KEY)
//                        .build();
//
////                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=77017,us&mode=json&units=metric&cnt=7";
////                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
////                URL url = new URL(baseUrl.concat(apiKey));
//
//                URL url = new URL(builtUri.toString());
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//
//                try {
//                    return getWeatherDataFromJson(forecastJsonStr,numDays);
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG,e.getMessage());
//                    e.printStackTrace();
//                }
//
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error reading from URL ", e);
//                // If the code didn't successfully get the weather data, there's no point in attemping
//                // to parse it.
//                return null;
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//            return null;
//        }
//
//        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//        @Override
//        protected void onPostExecute(String[] strings) {
//            if(strings!=null)
//            {
//                mForecastAdapter.clear();
//                mForecastAdapter.addAll(strings);
//                /*
//                for(string s:strings)
//                    mForecastAdapter.add(s);
//                 */
//            }
//            //Build adapter back from scratch -> not good
////            List<String> weekForecast = new ArrayList<String>(Arrays.asList(strings));
////            mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forcast,R.id.list_item_forecast_textview,weekForecast);
////            ListView listView = (ListView) getView().findViewById(R.id.listview_forcast);
////            listView.setAdapter(mForecastAdapter);
//
//
//        }
//
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low) {
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//
//            String[] resultStrs = new String[numDays];
//            for(int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime;
//                // Cheating to convert this to UTC time, which is what we want anyhow
//                dateTime = dayTime.setJulianDay(julianStartDay+i);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            return resultStrs;
//
//        }
//    }
}
