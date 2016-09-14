package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate activity_main layout in THIS ACTIVITY
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            //ADD A FRAGMENT TO THIS ACTIVITY
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //getMenuInflater().inflate(R.menu.map, menu);
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
            //Toast.makeText(this,"Called from main setting",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id==R.id.map_location)
        {
            openLocationInMap();
        }



        return super.onOptionsItemSelected(item);
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
