package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.android.sunshine.app.service.SunshineService;

import static com.example.android.sunshine.app.data.WeatherContract.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private boolean isTwoPane = false;
    private static final int WEATHER_LOADER_ID = 1;
    private static final String POSITION_KEY = "Position";
    private int mSelectedItemPosition;
    private ForecastAdapter adapter;
    private ListView listView;

    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onLocationChanged() {
        fetchForecast();
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    public void setIsTwoPane(boolean isTwoPane) {
        this.isTwoPane = isTwoPane;
        if (adapter != null)
            adapter.setSpecialTodayView(!isTwoPane);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            fetchForecast();
            return true;
        } else if (id == R.id.show_preferred_location) {
            showPreferredLocationOnMap();
            return true;
        } else {
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPreferredLocationOnMap() {
        String city = Utility.getPreferredLocation(getActivity());
        Uri intentUri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", city).build();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.e(ForecastFragment.class.getSimpleName(),
                    "Could not resolve activity for ACTION_VIEW intent.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mSelectedItemPosition = savedInstanceState.getInt(POSITION_KEY);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        adapter = new ForecastAdapter(getActivity(), null, 0);
        adapter.setSpecialTodayView(!isTwoPane);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getActivity() instanceof Callback) {
                    Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(
                            WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                                    cursor.getLong(ForecastAdapter.COL_WEATHER_DATE)));
                }
                mSelectedItemPosition = i;
            }
        });
        return rootView;
    }

    private void fetchForecast() {
        Intent sunshineServiceIntent = new Intent(getActivity(), SunshineService.class);
        sunshineServiceIntent.putExtra(SunshineService.LOCATION, Utility.getPreferredLocation(getActivity()));
        getActivity().startService(sunshineServiceIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedItemPosition != ListView.INVALID_POSITION)
            outState.putInt(POSITION_KEY, mSelectedItemPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherEntry.
                buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (listView != null)
            listView.smoothScrollToPosition(mSelectedItemPosition);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }
}