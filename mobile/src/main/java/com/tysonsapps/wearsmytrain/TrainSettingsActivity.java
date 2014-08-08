package com.tysonsapps.wearsmytrain;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tysonsapps.wearsmytrain.async.FetchJSONTask;
import com.tysonsapps.wearsmytrain.async.OnTaskCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferencesFactory;


public class TrainSettingsActivity extends Activity implements OnTaskCompleted{
    private Spinner mHomeStationSpinner;
    private Spinner mHomeEndOfLineStationSpinner;

    private Spinner mWorkStationSpinner;
    private Spinner mWorkEndOfLineStationSpinner;

    private boolean mUserIsInteracting;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public static final String HOME_STATION_KEY = "HOME_STATION_KEY";
    public static final String HOME_END_OF_LINE_STATION= "HOME_END_OF_LINE_STATION";

    public static final String WORK_STATION_KEY = "WORK_STATION_KEY";
    public static final String WORK_END_OF_LINE_STATION= "WORK_END_OF_LINE_STATION";

    private static final String STATIONS = "JStations";

    public static final String BASE_URL = "http://api.wmata.com/Rail.svc/json/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mUserIsInteracting = false;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();


        mWorkStationSpinner = (Spinner) findViewById(R.id.workSpinner);
        mWorkEndOfLineStationSpinner = (Spinner) findViewById(R.id.workEndOfLineSpinner);

        mHomeStationSpinner = (Spinner) findViewById(R.id.homeSpinner);
        mHomeEndOfLineStationSpinner = (Spinner) findViewById(R.id.homeEndOfLineSpinner);


        FetchJSONTask fetchStationsTask = new FetchJSONTask(this,STATIONS);
        fetchStationsTask.execute(BASE_URL + STATIONS + Constants.WMATA_API_KEY);

    }

    @Override
    public void onTaskCompleted(JSONObject result, String taskType) {
        if(result == null){
            return;
        }
        if(taskType.equals(STATIONS)){
            List<SpinnerItem> items = new ArrayList<SpinnerItem>();
            try {
                JSONArray stationsArray = result.getJSONArray("Stations");
                for(int i = 0; i < stationsArray.length(); i++){
                    items.add(new SpinnerItem(stationsArray.getJSONObject(i).getString("Name"), stationsArray.getJSONObject(i).getString("Code")));
                }

                Collections.sort(items);

                final ArrayAdapter homeStationsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
                final ArrayAdapter homeEndOfLineStationsAdapter= new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);

                final ArrayAdapter workStationsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
                final ArrayAdapter workEndOfLineStationsAdapter= new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);

                //home station

                mHomeStationSpinner.setAdapter(homeStationsAdapter);

                mHomeStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        SpinnerItem item = (SpinnerItem) homeStationsAdapter.getItem(position);

                        if(mUserIsInteracting) {
                            mEditor.putString(HOME_STATION_KEY, item.getId());
                            mEditor.commit();
                        }


                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {  }
                });

                String preferredHomeStation = mSharedPreferences.getString(HOME_STATION_KEY,"");
                for(int i = 0; i < items.size(); i++){
                    if(items.get(i).getId().equals(preferredHomeStation)){
                        mHomeStationSpinner.setSelection(i);
                    }
                }

                //home end of line station
                mHomeEndOfLineStationSpinner.setAdapter(homeEndOfLineStationsAdapter);

                mHomeEndOfLineStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        SpinnerItem item = (SpinnerItem) homeEndOfLineStationsAdapter.getItem(position);

                        if(mUserIsInteracting) {
                            mEditor.putString(HOME_END_OF_LINE_STATION, item.getId());
                            mEditor.commit();
                        }


                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {  }
                });

                String preferredEndOfLineHomeStation = mSharedPreferences.getString(HOME_END_OF_LINE_STATION,"");
                for(int i = 0; i < items.size(); i++){
                    if(items.get(i).getId().equals(preferredEndOfLineHomeStation)){
                        mHomeEndOfLineStationSpinner.setSelection(i);
                    }
                }

                //work station

                mWorkStationSpinner.setAdapter(workStationsAdapter);

                mWorkStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        SpinnerItem item = (SpinnerItem) workStationsAdapter.getItem(position);

                        if(mUserIsInteracting) {
                            mEditor.putString(WORK_STATION_KEY, item.getId());
                            mEditor.commit();
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {  }
                });

                String preferredWorkStation = mSharedPreferences.getString(WORK_STATION_KEY,"");
                for(int i = 0; i < items.size(); i++){
                    if(items.get(i).getId().equals(preferredWorkStation)){
                        mWorkStationSpinner.setSelection(i);
                    }
                }

                //work end of line station
                mWorkEndOfLineStationSpinner.setAdapter(workEndOfLineStationsAdapter);

                mWorkEndOfLineStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        SpinnerItem item = (SpinnerItem) workEndOfLineStationsAdapter.getItem(position);

                        if(mUserIsInteracting) {
                            mEditor.putString(WORK_END_OF_LINE_STATION, item.getId());
                            mEditor.commit();
                        }


                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {  }
                });

                String preferredEndOfLineWorkStation = mSharedPreferences.getString(WORK_END_OF_LINE_STATION,"");
                for(int i = 0; i < items.size(); i++){
                    if(items.get(i).getId().equals(preferredEndOfLineWorkStation)){
                        mWorkEndOfLineStationSpinner.setSelection(i);
                    }
                }

            } catch (JSONException e) {
                return;
            }
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        mUserIsInteracting = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


}
