package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TODAY = 0;
    private final int VIEW_FUTURE = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext,high, isMetric) + "/" + Utility.formatTemperature(mContext,low, isMetric);
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
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch (viewType)
        {
            case VIEW_TODAY:
                layoutId=R.layout.list_item_forecast_today;
                break;
            case VIEW_FUTURE:
                layoutId= R.layout.list_item_forcast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder); //set tag of the view
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
        Binding the values in the cursor to the view
     */


    //normally this will return 1, for 1 layout
    @Override
    public int getViewTypeCount() {
//        return super.getViewTypeCount();
        return 2; //two different layouts
    }

    @Override
    public int getItemViewType(int position) {
        //if position of the item on the list is - -> today view layout
        return (position ==0 ) ? VIEW_TODAY : VIEW_FUTURE;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(ForecastAdapter.class.getSimpleName(), "in bindView. Cursor position: " + cursor.getPosition());
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

//        TextView tv = (TextView)view;
//        tv.setText(convertCursorRowToUXFormat(cursor));


        ViewHolder viewHolder = (ViewHolder)view.getTag();

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        ImageView iconView = viewHolder.iconView;
        iconView.setImageResource(R.drawable.ic_launcher);

        // TODO Read date from cursor
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = viewHolder.dateView;
        dateView.setText(Utility.getFriendlyDayString(context, date));


        // TODO Read weather forecast from cursor
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView forecastView = viewHolder.descriptionView;
        forecastView.setText(forecast);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = viewHolder.highTempView;
        highView.setText(Utility.formatTemperature(context,high, isMetric));

        // TODO Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = viewHolder.lowTempView;
        lowView.setText(Utility.formatTemperature(context,low, isMetric));
    }





}