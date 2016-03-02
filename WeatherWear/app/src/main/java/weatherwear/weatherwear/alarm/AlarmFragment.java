package weatherwear.weatherwear.alarm;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import weatherwear.weatherwear.R;

/**
 * Created by Emily on 2/27/16.
 * Displays alarms
 */
public class AlarmFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<AlarmModel>> {
    private static final int ADD_ID = 0;
    public static final String DATE_FORMAT = "h:mm a";
    public static final String TIME_KEY = "time";
    public static final String SUN_KEY = "sun";
    public static final String MON_KEY = "mon";
    public static final String TUES_KEY = "tues";
    public static final String WED_KEY = "wed";
    public static final String THURS_KEY = "thurs";
    public static final String FRI_KEY = "fri";
    public static final String SAT_KEY = "sat";
    public static final String REPEAT_KEY = "repeat";
    public static final String EDIT_KEY = "edit";
    public static final String ID_KEY = "id";

    private static AlarmDatabaseHelper mDbHelper;
    private static ArrayAdapter<AlarmModel> mAlarmAdapter;
    private static Context mContext;
    public static LoaderManager loaderManager;
    public static int onCreateCheck=0;
    private static ArrayList<AlarmModel> mAlarmList = new ArrayList<AlarmModel>();

    @Override
    public void onResume() {
        super.onResume();
        // Re-query in case the data base has changed.
        requeryAlarms(mContext);
        AlarmScheduler.setSchedule(getActivity());
    }

    private void requeryAlarms(Context mContext) {
        if (mDbHelper == null) {;
            mDbHelper = new AlarmDatabaseHelper(mContext);
            mAlarmAdapter = new AlarmEntriesAdapter(this,mContext);
            loaderManager = getActivity().getLoaderManager();
            setListAdapter(mAlarmAdapter);
        }
        if(onCreateCheck==1){
            onCreateCheck=0;
        } else {
            loaderManager.initLoader(1, null, this).forceLoad();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_fragment, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.my_alarms);
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        // Open data base for operations.
        mDbHelper = new AlarmDatabaseHelper(mContext);
        loaderManager = getActivity().getLoaderManager();
        // Instantiate our customized array adapter
        mAlarmAdapter = new AlarmEntriesAdapter(this,mContext);
        // Set the adapter to the listview
        setListAdapter(mAlarmAdapter);
        loaderManager.initLoader(1, null, this).forceLoad();
        onCreateCheck=1;
        setHasOptionsMenu(true);
        return view;
    }

    //opens up settings
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AlarmAlertManager aAManager = new AlarmAlertManager();
        if(aAManager.isPlaying()){
            aAManager.stopAlerts();
        }
        Intent intent = new Intent(mContext,AlarmSettingsActivity.class);
        //previously saved settings to display
        Bundle extras = new Bundle();

        // get the alarm corresponding to history
        AlarmModel alarmModel = mAlarmAdapter.getItem(position);
        // displaying history
        extras.putLong(TIME_KEY, alarmModel.getTimeInMillis());
        extras.putBoolean(SUN_KEY, alarmModel.getSun());
        extras.putBoolean(MON_KEY, alarmModel.getMon());
        extras.putBoolean(TUES_KEY, alarmModel.getTues());
        extras.putBoolean(WED_KEY, alarmModel.getWed());
        extras.putBoolean(THURS_KEY, alarmModel.getThurs());
        extras.putBoolean(FRI_KEY, alarmModel.getFri());
        extras.putBoolean(SAT_KEY, alarmModel.getSat());
        extras.putBoolean(REPEAT_KEY, alarmModel.getRepeat());
        extras.putBoolean(EDIT_KEY, true);
        extras.putLong(ID_KEY, alarmModel.getId());

        intent.putExtras(extras);
        startActivity(intent);
    }

    // From 1970 epoch time in seconds to something like "10/24/2012"
    public static String parseTime(long msTime) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(msTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuitem;
        menuitem = menu.add(Menu.NONE, ADD_ID, ADD_ID, getString(R.string.plus_btn));
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ADD_ID:
                startActivity(new Intent(getActivity(),AlarmSettingsActivity.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public Loader<ArrayList<AlarmModel>> onCreateLoader(int id, Bundle args) {
        return new AlarmLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AlarmModel>> loader, ArrayList<AlarmModel> data) {
        mAlarmList = data;
        mAlarmAdapter.clear();
        mAlarmAdapter.addAll(mAlarmList);
        mAlarmAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AlarmModel>> loader) {
        mAlarmAdapter.clear();
        mAlarmAdapter.addAll(mAlarmList);
        mAlarmAdapter.notifyDataSetChanged();
    }

    // Subclass of ArrayAdapter to display interpreted database row values in
    // customized list view.
    private class AlarmEntriesAdapter extends ArrayAdapter<AlarmModel> {
        final /* synthetic */ AlarmFragment this_0;

        public AlarmEntriesAdapter(AlarmFragment alarmFragment, Context context) {
            super(context, R.layout.alarm_list_layout);
            this.this_0 = alarmFragment;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View listItemView = convertView;
            if (null == convertView) {
                // we need to check if the convertView is null. If it's null,
                // then inflate it.
                listItemView = inflater.inflate(R.layout.alarm_list_layout, parent, false);
            }

            TextView titleView = (TextView) listItemView.findViewById(R.id.titleText);
            TextView subtitleView = (TextView) listItemView.findViewById(R.id.subtitle);
            Switch switchState = (Switch) listItemView.findViewById(R.id.switchState);
            TextView switchView = (TextView) listItemView.findViewById(R.id.switchText);

            final AlarmModel alarm = getItem(position);

            //parse data to readable format
            String time = parseTime(alarm.getTimeInMillis());

            // Set text on the view.
            titleView.setText(time);
            subtitleView.setText(alarm.weeklyInfo());

            switchState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    AlarmAlertManager aAManager = new AlarmAlertManager();
                    if(aAManager.isPlaying()){
                        aAManager.stopAlerts();
                    }
                    if (isChecked) {
                        alarm.setIsOn(true);
                        mDbHelper.onUpdate(alarm);
                        AlarmScheduler.setSchedule(getActivity());
                    } else {
                        alarm.setIsOn(false);
                        mDbHelper.onUpdate(alarm);
                        AlarmScheduler.setSchedule(getActivity());
                    }
                    mAlarmAdapter.notifyDataSetChanged();
                }
            });
            if(alarm.getIsOn()){
                switchView.setText("ON");
                switchState.setChecked(true);
            } else {
                switchView.setText("OFF");
                switchState.setChecked(false);
            }
            return listItemView;
        }
    }

    private static class AlarmLoader extends AsyncTaskLoader<ArrayList<AlarmModel>> {
        public Context mContext;
        public AlarmLoader(Context context) {
            super(context);
            mContext = context;
        }
        @Override
        protected void onStartLoading() {
            forceLoad();
        }
        @Override
        public ArrayList<AlarmModel> loadInBackground() {
            mDbHelper = new AlarmDatabaseHelper(mContext);
            mAlarmList = mDbHelper.fetchEntries();
            return mAlarmList;
        }
    }

}
