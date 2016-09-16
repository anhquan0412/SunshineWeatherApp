package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {



    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
        Date - Weather -- High/Low
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor


        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
        They create duplicates of the same layout to put into the list view.
        This is where you return what layout is going to be duplicated.

        New view is created when it's a new loader...?
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(ForecastAdapter.class.getSimpleName(), "in newView. New view is created");
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forcast, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
        Binding the values in the cursor to the view
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(ForecastAdapter.class.getSimpleName(), "in bindView. Cursor position: " + cursor.getPosition());
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}