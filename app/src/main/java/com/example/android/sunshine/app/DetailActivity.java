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
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    private static final int WEATHER_LOADER_ID = 10;

    private static String[] DETAILED_FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor>
    {

        ShareActionProvider shareActionProvider;
        final String SHARE_HASHTAG = "#SunshineApp";
        private final static String DETAILED_FORECAST_URI = "uri";
        String weatherData;
        private View rootView;

        public PlaceholderFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle uriData = new Bundle();
            uriData.putParcelable(DETAILED_FORECAST_URI, getActivity().getIntent().getData());
            getLoaderManager().initLoader(WEATHER_LOADER_ID, uriData, this);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail_fragment, menu);
            MenuItem shareItem = menu.findItem(R.id.share_menu_item);
            shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
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

            return rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        }

        private void populateData(Cursor cursor) {
            TextView textView = (TextView)rootView.findViewById(R.id.detail_text);

            if (cursor.moveToNext()) {
                boolean isMetric = Utility.isMetric(getActivity());
                weatherData = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE)) +
                        ": " + Utility.formatTemperature(cursor.getInt(COL_WEATHER_MAX_TEMP), isMetric) +
                        "/" + Utility.formatTemperature(cursor.getInt(COL_WEATHER_MIN_TEMP), isMetric) +
                        " - " + cursor.getString(COL_WEATHER_DESC);
                textView.setText(weatherData);
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
            return new CursorLoader(getActivity(), (Uri)args.get(DETAILED_FORECAST_URI),
                    DETAILED_FORECAST_COLUMNS, null, null, null);
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
}