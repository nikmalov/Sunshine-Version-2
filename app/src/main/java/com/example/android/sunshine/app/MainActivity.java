package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.weather_detail_container, new DetailedForecastFragment(),
                                DETAIL_FRAGMENT_TAG).commit();
            }
        } else {
            mTwoPane = false;
        }
        mLocation = Utility.getPreferredLocation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff)
                ff.onLocationChanged();
            DetailedForecastFragment df = (DetailedForecastFragment)getSupportFragmentManager().
                    findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (null != df)
                df.onLocationChanged(location);
            mLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    public void onItemSelected(Uri dateUri) {
        if (!mTwoPane) {
            Intent detailActivityIntent = new Intent(this, DetailActivity.class).setData(dateUri);
            startActivity(detailActivityIntent);
        } else {
            DetailedForecastFragment ff = new DetailedForecastFragment();
            Bundle data = new Bundle();
            data.putParcelable(DetailedForecastFragment.DETAILED_FORECAST_URI, dateUri);
            ff.setArguments(data);
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.weather_detail_container, ff, DETAIL_FRAGMENT_TAG).commit();
        }
    }
}
