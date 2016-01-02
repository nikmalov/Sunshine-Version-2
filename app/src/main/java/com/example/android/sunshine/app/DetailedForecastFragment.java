package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by nikmalov.
 */
public class DetailedForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final int WEATHER_LOADER_ID = 10;

    private static String[] DETAILED_FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_WIND = 6;
    public static final int COL_WEATHER_WIND_DIRECTION = 7;
    public static final int COL_WEATHER_PRESSURE = 8;
    public static final int COL_LOCATION_SETTING = 9;
    public static final int COL_WEATHER_CONDITION_ID = 10;

    ShareActionProvider shareActionProvider;
    final String SHARE_HASHTAG = "#SunshineApp";
    public final static String DETAILED_FORECAST_URI = "uri";
    String weatherData;
    private View rootView;
    private Uri mUri;

    private TextView mDayTextView;
    private TextView mDateTextView;
    private TextView mMaxTemperatureTextView;
    private TextView mMinTemperatureTextView;
    private TextView mHumidityTextView;
    private TextView mWindTextView;
    private TextView mPressureTextView;
    private ImageView mIconView;
    private TextView mForecastTextView;

    public DetailedForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem shareItem = menu.findItem(R.id.share_menu_item);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (weatherData != null)
            shareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share_menu_item) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail_alias, container, false);
        mDayTextView = (TextView)rootView.findViewById(R.id.details_item_day_textview);
        mDateTextView = (TextView)rootView.findViewById(R.id.details_item_date_textview);
        mMaxTemperatureTextView = (TextView)rootView.findViewById(R.id.details_item_high_textview);
        mMinTemperatureTextView = (TextView)rootView.findViewById(R.id.details_item_low_textview);
        mHumidityTextView = (TextView)rootView.findViewById(R.id.details_item_humidity);
        mWindTextView = (TextView)rootView.findViewById(R.id.details_item_wind);
        mPressureTextView = (TextView)rootView.findViewById(R.id.details_item_pressure);
        mIconView = (ImageView)rootView.findViewById(R.id.details_item_icon);
        mForecastTextView = (TextView)rootView.findViewById(R.id.details_item_forecast);
        return rootView;
    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
        }

    }

    private void populateData(Cursor cursor) {
        if (cursor.moveToFirst()) {
            boolean isMetric = Utility.isMetric(getActivity());
            long dateInMillis = cursor.getLong(COL_WEATHER_DATE);
            mDayTextView.setText(Utility.getDayName(getActivity(), dateInMillis));
            mDateTextView.setText(Utility.getFormattedMonthDay(getActivity(), dateInMillis));
            mMaxTemperatureTextView.setText(Utility.formatTemperature(
                    getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
            mMinTemperatureTextView.setText(Utility.formatTemperature(
                    getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
            mHumidityTextView.setText(getActivity().getString(R.string.format_humidity,
                    cursor.getFloat(COL_WEATHER_HUMIDITY)));
            mWindTextView.setText(Utility.getFormattedWind(getActivity(),
                    cursor.getFloat(COL_WEATHER_WIND),
                    cursor.getFloat(COL_WEATHER_WIND_DIRECTION)));
            mPressureTextView.setText(getActivity().getString(R.string.format_pressure,
                    cursor.getFloat(COL_WEATHER_PRESSURE)));
            int weatherId = cursor.getInt(COL_WEATHER_CONDITION_ID);
            int iconResId = Utility.getArtResourceForWeatherCondition(weatherId);
            if (iconResId != -1)
                mIconView.setImageDrawable(getResources().getDrawable(iconResId));
            mIconView.setContentDescription(cursor.getString(COL_WEATHER_DESC));
            mForecastTextView.setText(cursor.getString(COL_WEATHER_DESC));
        }

    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, weatherData + SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getArguments() != null)
            mUri = (Uri)getArguments().get(DETAILED_FORECAST_URI);
        if (mUri == null)
            return null;
        return new CursorLoader(getActivity(), mUri, DETAILED_FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        populateData(data);
        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}