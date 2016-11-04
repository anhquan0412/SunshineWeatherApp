package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.data.WeatherContract;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {


    //to store our current known location
    private String mLocation;

    //a fragment tag:
//    constant String we can use to tag a fragment within the fragment manager so we can easily look it up later.
//    private final String FORECASTFRAGMENT_TAG = "FFTAG"; //no need tag for this shit, since forecast fragment will never be created explicitly

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(MainActivity.class.getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);
        //inflate activity_main layout in THIS ACTIVITY
        setContentView(R.layout.activity_main);


        //Since we already declare static fragment in activity_main (in both handheld and tablet), we don't need to manually do it here
//        if (savedInstanceState == null)
//        {
//            getSupportFragmentManager().beginTransaction().add(R.id.container, new ForecastFragment()).commit();
//        }
        if(findViewById(R.id.weather_detail_container) != null)
        {
            //only layout-sw600dp has weather_detail_container
            mTwoPane = true;

            //show  detail view in this activity
            // only do this when savedInstanceState stores nothing. If savedInstanceState is initialized, system will handle restoring the fragment
            if(savedInstanceState== null)
            {
                DetailsFragment df = new DetailsFragment();
                Bundle arg = new Bundle();
                Uri temp = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation,Utility.getToday());
                arg.putParcelable(DetailsFragment.DETAIL_URI, temp);
                df.setArguments(arg);

                getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG).commit();
            }

        }
        else
            mTwoPane = false;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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


        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id==R.id.map_location)
        {
            openLocationInMap();
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(MainActivity.class.getSimpleName(), "onResume");
        super.onResume();

        String newLocation = Utility.getPreferredLocation(this);
        if(newLocation!=null && !newLocation.equals(mLocation)) //location has changed
        {
//            Get the ForecastFragment using the tag
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if(ff!=null) {
                // updateWeather() and restart loader -> now loader can link to new data fetched from openweather API
                ff.onLocationChanged();
            }
            DetailsFragment df = (DetailsFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(df!=null)
            {
                df.onLocationChanged(newLocation);
            }
            mLocation = newLocation;
        }
    }

    private void openLocationInMap()
    {
//        SharedPreferences sharedPref= PreferenceManager.getDefaultSharedPreferences(this);
//        String locationValue = sharedPref.getString(getString(R.string.pref_location_key),
//                getString(R.string.pref_location_default));

        String location = Utility.getPreferredLocation(this);

        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else
        {
            Log.e(MainActivity.class.getSimpleName(),"Error opening location " + location);
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {

        /** pseudo code:
        if one pane, create intent and start DetailActivity
         if 2 pane, create new detail fragment given the dateUri and replace the old detail fragment withit

         **/
        if(mTwoPane)
        {
            //create arguments
            Bundle args = new Bundle();
            args.putParcelable(DetailsFragment.DETAIL_URI,dateUri);
            DetailsFragment df = new DetailsFragment();

            //set arguments to new DetailsFragment obj
            df.setArguments(args);

            //replace old DetailsFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG).commit();

        }
        else
        {
            //Create intent with dateUri and starts details activity
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }




    }
}
