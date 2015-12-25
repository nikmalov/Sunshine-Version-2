package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final static String METRIC_UNITS = "metric";
    private ForecastAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchForecast();
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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.
                buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        Cursor cursor = getActivity().getContentResolver().query(
                weatherForLocationUri, null, null, null, sortOrder);
        adapter = new ForecastAdapter(getActivity(), cursor, 0);
        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent detailViewIntent = new Intent(getActivity(), DetailActivity.class);
//                detailViewIntent.putExtra(DetailActivity.FORECAST_DATA,
//                        adapterView.getItemAtPosition(i).toString());
//                startActivity(detailViewIntent);
//            }
//        });
        return rootView;
    }

    private void fetchForecast() {
        new FetchWeatherTask(getActivity()).execute(Utility.getPreferredLocation(getActivity()));
    }
}