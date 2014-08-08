package com.tysonsapps.wearsmytrain;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.tysonsapps.wearsmytrain.async.FetchJSONTask;
import com.tysonsapps.wearsmytrain.async.OnTaskCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WatchListenerService extends WearableListenerService implements OnTaskCompleted {
    private static final String START_ACTIVITY_PATH = "/fetch-train-times";
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mSharedPreferences;

    public static final String BASE_URL = "http://api.wmata.com/StationPrediction.svc/json/";
    private static final String PREDICTIONS = "GetPrediction";

    private String mNodeId;

    private boolean mMorning;


    @Override
    public void onCreate() {
        Log.d("OUTPUT", "Starting ResponseListenerService (PHONE)!");
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        Log.d("OUTPUT", "Message received on PHONE!!");
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)){
            Log.d("Message Path", messageEvent.getPath());
            String response = new String(messageEvent.getData());
            Log.d("Message Contents", response);

            mNodeId = messageEvent.getSourceNodeId();

            FetchJSONTask fetchTimePredictions = new FetchJSONTask(this,PREDICTIONS);

            Date date = new Date();
            Calendar calendar = GregorianCalendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            String station;
            if(hour < 12){ //AM
                mMorning = true;
                station = mSharedPreferences.getString(TrainSettingsActivity.HOME_STATION_KEY,"");
            }
            else{ //PM
                mMorning = false;
                station = mSharedPreferences.getString(TrainSettingsActivity.WORK_STATION_KEY,"");
            }



            fetchTimePredictions.execute(BASE_URL + PREDICTIONS + "/" + station + Constants.WMATA_API_KEY);

        }
        stopSelf();
    }

    @Override
    public void onTaskCompleted(JSONObject result, String taskType) {
        String endOfLineStation;
        if(mMorning){
            endOfLineStation = mSharedPreferences.getString(TrainSettingsActivity.WORK_END_OF_LINE_STATION,"");
        }
        else{
            endOfLineStation = mSharedPreferences.getString(TrainSettingsActivity.HOME_END_OF_LINE_STATION,"");
        }

        try {
            String jsonForWatch;

            if(result != null) {
                JSONArray trainPredictions = result.getJSONArray("Trains");

                JSONObject nextTrainJSON = null;

                for (int i = 0; i < trainPredictions.length(); i++) {
                    JSONObject obj = trainPredictions.getJSONObject(i);
                    if (obj.getString("DestinationCode").equals(endOfLineStation)) {
                        nextTrainJSON = obj;
                        break;
                    }
                }


                if (nextTrainJSON != null) {
                    jsonForWatch = nextTrainJSON.toString();
                } else {
                    jsonForWatch = ""; //no trains...
                }
            }
            else{
                jsonForWatch = ""; //JSON didn't come back from API correctly...
            }

            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNodeId, START_ACTIVITY_PATH, jsonForWatch.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.d("PHONE OUTPUT","Connection failed (sync)!");
                            } else {
                                Log.d("PHONE OUTPUT","Successfully sent train times data!");
                            }
                        }
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
}