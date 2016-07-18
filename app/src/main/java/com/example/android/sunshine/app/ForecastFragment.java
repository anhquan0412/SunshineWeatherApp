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

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;



    public ForecastFragment() {

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

        if (id==R.id.action_refresh)
        {
            updateWeather();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    //helper method to get location preference value from sharedpreference
    private void updateWeather()
    {
        FetchWeatherTask task = new FetchWeatherTask();

        //get shared preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        //get value based on preference key and value (if no value, use preference default value)
        String location = sharedPref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        //get unit's preference value from shared preference
        String unit = sharedPref.getString(getString(R.string.pref_unit_key),getString(R.string.pref_unit_default));
        Log.e(ForecastFragment.class.getSimpleName(),location + " " + unit);
        task.execute(location,unit);
    }


    //override onStart, so updateWeather is called whenever the fragment starts
    // THIS FUNCTION CAN BE CALLED MULTIPLE TIMES (WHEN FRAGMENT STARTS)
    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }


    /*
    Called to have the fragment instantiate its user interface view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the fragment_main layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

//         Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        //empty array list
        List<String> weekForecast = new ArrayList<String>();

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forcast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast); //passing empty array list



        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forcast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getActivity(),adapterView.getItemAtPosition(i).toString(),Toast.LENGTH_SHORT).show();
                String content = adapterView.getItemAtPosition(i).toString();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(detailIntent);
            }
        });
        return rootView;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */



    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            if(params.length==0) return null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            final String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //Use URI builder to build the URI with custom key + parameter
                //to build http://api.openweathermap.org/data/2.5/forecast/daily?q=77017,us&mode=json&units=metric&cnt=7&APPID=YOURKEYHERE
                Uri builtUri = Uri.parse(baseURL).buildUpon()
                        .appendQueryParameter("q",params[0])
                        .appendQueryParameter("mode",format)
                        .appendQueryParameter("units",params[1])
                        .appendQueryParameter("cnt",Integer.toString(numDays))
                        .appendQueryParameter("APPID",BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

//                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=77017,us&mode=json&units=metric&cnt=7";
//                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
//                URL url = new URL(baseUrl.concat(apiKey));

                URL url = new URL(builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                try {
                    return getWeatherDataFromJson(forecastJsonStr,numDays);
                } catch (JSONException e) {
                    Log.e(LOG_TAG,e.getMessage());
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error reading from URL ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(String[] strings) {
            if(strings!=null)
            {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(strings);
                /*
                for(string s:strings)
                    mForecastAdapter.add(s);
                 */
            }
            //Build adapter back from scratch -> not good
//            List<String> weekForecast = new ArrayList<String>(Arrays.asList(strings));
//            mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forcast,R.id.list_item_forecast_textview,weekForecast);
//            ListView listView = (ListView) getView().findViewById(R.id.listview_forcast);
//            listView.setAdapter(mForecastAdapter);


        }

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;

        }
    }
}
