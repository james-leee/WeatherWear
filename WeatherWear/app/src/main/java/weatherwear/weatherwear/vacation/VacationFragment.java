package weatherwear.weatherwear.vacation;

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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import at.markushi.ui.CircleButton;
import weatherwear.weatherwear.R;
import weatherwear.weatherwear.Utils;

public class VacationFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<VacationModel>>{
    private final static int ADD_ID = 0;
    private static final int LOADER_ID = 2;

    private static VacationDatabaseHelper mDbHelper;
    private static ArrayList<VacationModel> vacationList = new ArrayList<VacationModel>();
    private static ArrayAdapter<VacationModel> mVacationAdapter;
    private static Context mContext;

    public static LoaderManager loaderManager;
    public static int onCreateCheck = 0;

    public VacationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initializes the view and sets the title bar
        View view = inflater.inflate(R.layout.vacation_fragment, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.my_vacations);
        setHasOptionsMenu(true);

        mContext = getActivity();

        // Open data base for operations.
        mDbHelper = new VacationDatabaseHelper(mContext);
        loaderManager = getActivity().getLoaderManager();
        // Instantiate our customized array adapter
        mVacationAdapter = new VacationEntriesAdapter(mContext);
        // Set the adapter to the listview
        setListAdapter(mVacationAdapter);
        loaderManager.initLoader(LOADER_ID, null, this).forceLoad();
        onCreateCheck = 1;
        setHasOptionsMenu(true);
        return view;
    }

    //opens up settings
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(mContext,VacationCreatorActivity.class);
        //previously saved settings to display
        Bundle extras = new Bundle();
        // get the Vacation corresponding to history
        VacationModel vacationModel = mVacationAdapter.getItem(position);
        // displaying history
        extras.putLong(VacationCreatorActivity.START_KEY, vacationModel.getStartInMillis());
        extras.putLong(VacationCreatorActivity.END_KEY, vacationModel.getEndInMillis());
        extras.putLong(VacationCreatorActivity.ID_KEY, vacationModel.getId());
        extras.putString(VacationCreatorActivity.ZIP_CODE_KEY, vacationModel.getZipCode());
        extras.putString(VacationCreatorActivity.NAME_KEY, vacationModel.getName());
        extras.putBoolean(VacationCreatorActivity.HISTORY_KEY, true);
        extras.putLong(VacationOutfitsActivity.DAY_ONE_KEY, vacationModel.getDayOne());
        extras.putLong(VacationOutfitsActivity.DAY_TWO_KEY, vacationModel.getDayTwo());
        extras.putLong(VacationOutfitsActivity.DAY_THREE_KEY, vacationModel.getDayThree());
        extras.putLong(VacationOutfitsActivity.DAY_FOUR_KEY, vacationModel.getDayFour());
        extras.putLong(VacationOutfitsActivity.DAY_FIVE_KEY, vacationModel.getDayFive());
        intent.putExtras(extras);
        startActivity(intent);
    }

    // Adds 'Add' button to the menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuitem;
        menuitem = menu.add(Menu.NONE, ADD_ID, ADD_ID, getString(R.string.plus_btn));
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    // Handles pressing the 'Add' button and redirecting VacationCreatorActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ADD_ID:
                startActivity(new Intent(getActivity(), VacationCreatorActivity.class));
                return true;
            default:
                return false;
        }
    }

    // Handles all Async Loading
    @Override
    public Loader<ArrayList<VacationModel>> onCreateLoader(int id, Bundle args) {
        return new VacationLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<VacationModel>> loader, ArrayList<VacationModel> data) {
        vacationList = data;
        mVacationAdapter.clear();
        mVacationAdapter.addAll(vacationList);
        mVacationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<VacationModel>> loader) {
        mVacationAdapter.clear();
        mVacationAdapter.addAll(vacationList);
        mVacationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-query in case the data base has changed.
        requeryVacations(mContext);
    }

    // Requeries the database for all of the vacations (if it may have changed)
    private void requeryVacations(Context mContext) {
        if (mDbHelper == null) {;
            mDbHelper = new VacationDatabaseHelper(mContext);
            mVacationAdapter = new VacationEntriesAdapter(mContext);
            loaderManager = getActivity().getLoaderManager();
            setListAdapter(mVacationAdapter);
        }

        if(onCreateCheck == 1){
            onCreateCheck = 0;
        } else {
            loaderManager.initLoader(LOADER_ID, null, this).forceLoad();
        }
    }

    // Subclass of ArrayAdapter to display interpreted database row values in
    // customized list view.
    private class VacationEntriesAdapter extends ArrayAdapter<VacationModel> {
        public VacationEntriesAdapter(Context context) {
            super(context, R.layout.vacation_list_layout);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View listItemView = convertView;
            // Optionally inflate if null
            if (listItemView == null) {
                listItemView = inflater.inflate(R.layout.vacation_list_layout, parent, false);
            }

            // Get all textviews and buttons
            TextView titleView = (TextView) listItemView.findViewById(R.id.vacation_titleTxt);
            TextView subtitleView = (TextView) listItemView.findViewById(R.id.vacation_subtitleTxt);
            at.markushi.ui.CircleButton button = (CircleButton) listItemView.findViewById(R.id.viewOutfitsButn);

            final VacationModel vacation = getItem(position);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Build the intent, and send it to the VacationOutfitsActivity
                    Intent i = new Intent(mContext, VacationOutfitsActivity.class);
                    i.putExtra(VacationOutfitsActivity.NAME_KEY, vacation.getName());
                    i.putExtra(VacationOutfitsActivity.ID_KEY, vacation.getId());
                    i.putExtra(VacationOutfitsActivity.ZIPCODE_KEY, vacation.getZipCode());
                    i.putExtra(VacationOutfitsActivity.START_KEY, vacation.getStartInMillis());
                    i.putExtra(VacationOutfitsActivity.END_KEY, vacation.getEndInMillis());
                    i.putExtra(VacationOutfitsActivity.DAYS_KEY,
                            Utils.getNumDays(vacation.getStartInMillis(), vacation.getEndInMillis()));
                    i.putExtra(VacationOutfitsActivity.DAY_ONE_KEY, vacation.getDayOne());
                    i.putExtra(VacationOutfitsActivity.DAY_TWO_KEY, vacation.getDayTwo());
                    i.putExtra(VacationOutfitsActivity.DAY_THREE_KEY, vacation.getDayThree());
                    i.putExtra(VacationOutfitsActivity.DAY_FOUR_KEY, vacation.getDayFour());
                    i.putExtra(VacationOutfitsActivity.DAY_FIVE_KEY, vacation.getDayFive());
                    startActivity(i);
                }
            });

            String subtitleDate = Utils.parseDate(vacation.getStartInMillis()) + " ~ "
                    + Utils.parseDate(vacation.getEndInMillis());

            // Set text on the view.
            titleView.setText(vacation.getName());
            subtitleView.setText(subtitleDate);

            return listItemView;
        }
    }

    // Async Task Loader to bring in all Vacation Models
    private static class VacationLoader extends AsyncTaskLoader<ArrayList<VacationModel>> {
        public Context mContext;
        public VacationLoader(Context context) {
            super(context);
            mContext = context;
        }
        @Override
        protected void onStartLoading() {
            forceLoad();
        }
        @Override
        public ArrayList<VacationModel> loadInBackground() {
            mDbHelper = new VacationDatabaseHelper(mContext);
            vacationList = mDbHelper.fetchEntries();
            return vacationList;
        }
    }

}
