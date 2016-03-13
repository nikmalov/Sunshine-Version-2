package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
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

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    private static final int TODAY_VIEW_TYPE_IDX = 0;
    private static final int FUTURE_VIEW_TYPE_IDX = 1;

    private boolean isSpecialTodayView;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void setSpecialTodayView(boolean isSpecialTodayView) {
        this.isSpecialTodayView = isSpecialTodayView;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && isSpecialTodayView ? TODAY_VIEW_TYPE_IDX : FUTURE_VIEW_TYPE_IDX;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = viewType == TODAY_VIEW_TYPE_IDX ?
                R.layout.list_item_forecast_today : R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        int iconResId = getItemViewType(cursor.getPosition()) == TODAY_VIEW_TYPE_IDX ?
                Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID)) :
                Utility.getIconResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID));
        if (iconResId != -1)
            viewHolder.iconView.setImageDrawable(mContext.getResources().getDrawable(iconResId));
        viewHolder.dateView.setText(
                Utility.getFriendlyDayString(mContext, cursor.getLong(COL_WEATHER_DATE)));
        viewHolder.forecastView.setText(cursor.getString(COL_WEATHER_DESC));
        viewHolder.highTemperatureView.setText(
                Utility.formatTemperature(mContext, cursor.getDouble(COL_WEATHER_MAX_TEMP)));
        viewHolder.lowTemperatureView.setText(
                Utility.formatTemperature(mContext, cursor.getDouble(COL_WEATHER_MIN_TEMP)));
    }

    static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView forecastView;
        public final TextView highTemperatureView;
        public final TextView lowTemperatureView;

        public ViewHolder(View view) {
                iconView = (ImageView)view.findViewById(R.id.list_item_icon);
                dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
                forecastView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
                highTemperatureView = (TextView)view.findViewById(R.id.list_item_high_textview);
                lowTemperatureView = (TextView)view.findViewById(R.id.list_item_low_textview);
        }
    }
}