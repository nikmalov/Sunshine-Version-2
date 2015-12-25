package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final static String METRIC_UNITS = "metric";
    private static SharedPreferences preferences;
    private ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        String city = getPreferredLocation();
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
        ArrayList<String> mockData = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview, mockData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent detailViewIntent = new Intent(getActivity(), DetailActivity.class);
                detailViewIntent.putExtra(DetailActivity.FORECAST_DATA,
                        adapterView.getItemAtPosition(i).toString());
                startActivity(detailViewIntent);
            }
        });
        return rootView;
    }

    private void fetchForecast() {
        new FetchWeatherTask(getActivity(), adapter).execute(getPreferredLocation());
    }

    private String getPreferredLocation() {
        return preferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
    }
}