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
        String city = preferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
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
        String city = preferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        new FetchWeatherTask().execute(city);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final static String DAILY_WEATHER_URL =
                "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private final static String LOCATION_ZIP_CODE = "zip";
        private final static String LOCATION_CITY_NAME = "q";
        private final static String MODE = "mode";
        private final static String JSON = "json";
        private final static String UNITS = "units";
        private final static String METRIC = "metric";
        private final static String DAYS_COUNT = "cnt";
        private final static int defaultDaysNumber = 7;
        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... strings) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                if (strings[0] == null || strings[0].isEmpty()) {
                    Log.e(TAG, "No url was provided.");
                    return null;
                }

                Uri uri = Uri.parse(DAILY_WEATHER_URL).buildUpon().
                        appendQueryParameter(LOCATION_CITY_NAME, strings[0]).
                        appendQueryParameter(MODE, JSON).
                        appendQueryParameter(UNITS, METRIC).
                        appendQueryParameter(DAYS_COUNT, Integer.toString(defaultDaysNumber)).
                        build();

                URL url = new URL(uri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            String[] result = new String[defaultDaysNumber];
            try {
                result = parseJsonResponse(forecastJsonStr, defaultDaysNumber);
            } catch (JSONException e) {
                Log.e(TAG, "JSON response failed to parse.");
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings == null)
                return;
            adapter.clear();
            adapter.addAll(strings);
        }

        private String[] parseJsonResponse(String jsonResponse, int daysCount) throws JSONException
        {
            final String weather = "weather";
            final String mainInfo = "main";
            final String tempInfo = "temp";
            final String minTemp = "min";
            final String maxTemp = "max";
            final String separator = " - ";

            String[] result = new String[daysCount];
            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray daysDataArray = jsonObject.getJSONArray("list");
            StringBuilder builder = new StringBuilder();
            JSONObject jsonDayObject;
            JSONObject tempObject;
            for (int i = 0; i < result.length; i++) {
                jsonDayObject = daysDataArray.optJSONObject(i);
                builder.append(getReadableDateString(dayTime.setJulianDay(julianStartDay+i))).
                        append(separator).
                        append(jsonDayObject.getJSONArray(weather).getJSONObject(0).getString(mainInfo)).
                        append(separator);
                tempObject = jsonDayObject.getJSONObject(tempInfo);
                builder.append(formatHighLows(
                        tempObject.getDouble(maxTemp), tempObject.getDouble(minTemp)));
                result[i] = builder.toString();
                builder.delete(0, builder.length());
            }
            return result;
        }

        private String getReadableDateString(long timeInMillis) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd");
            return dateFormat.format(timeInMillis);
        }

        private String formatHighLows(double high, double low) {
            String tempUnits = preferences.getString(getString(R.string.pref_temp_unit_key),
                    getString(R.string.pref_location_default));
            boolean isMetric = tempUnits == null || tempUnits.equalsIgnoreCase(METRIC_UNITS);
            long roundedHigh = isMetric ? Math.round(high) : Math.round(32 + 1.8 * high);
            long roundedLow = isMetric ? Math.round(low) : Math.round(32 + 1.8 * high);

            return roundedHigh + "/" + roundedLow;
        }
    }
}