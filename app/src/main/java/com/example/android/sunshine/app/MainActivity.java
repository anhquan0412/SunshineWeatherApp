package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {


    //to store our current known location
    private String mLocation;

    //a fragment tag:
//    constant String we can use to tag a fragment within the fragment manager so we can easily look it up later.
    private final String FORECASTFRAGMENT_TAG = "FFTAG";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(MainActivity.class.getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        //inflate activity_main layout in THIS ACTIVITY
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            //ADD A FRAGMENT TO THIS ACTIVITY
            getSupportFragmentManager().beginTransaction()
                    //ADD THE TAG IN FRAGMENT TRANSACTION
                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
                    .commit();
        }

        mLocation = Utility.getPreferredLocation(this);
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
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            if(ff!=null) {
                // updateWeather() and restart loader -> now loader can link to new data fetched from openweather API
                ff.onLocationChanged();
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

}
