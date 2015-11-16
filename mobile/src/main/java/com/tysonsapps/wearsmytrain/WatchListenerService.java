package com.tysonsapps.wearsmytrain;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class WatchListenerService extends WearableListenerService {
    private static final String START_ACTIVITY_PATH = "/fetch-train-times";
    private static final String TAG = "WatchListenerService";
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


            Ion.with(this)
                    .load(BASE_URL + PREDICTIONS + "/" + station + Constants.WMATA_API_KEY)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if (e == null) {
                                parseAndSendBackResultToWatch(result);
                            }
                        }
                    });

        }
        stopSelf();
    }

    public void parseAndSendBackResultToWatch(JsonObject result) {
        if(result == null || result.get("statusCode").getAsInt() == 401){
            Log.e(TAG,getString(R.string.invalid_api_key));
            return;
        }

        String endOfLineStation;
        if(mMorning){
            endOfLineStation = mSharedPreferences.getString(TrainSettingsActivity.WORK_END_OF_LINE_STATION,"");
        }
        else{
            endOfLineStation = mSharedPreferences.getString(TrainSettingsActivity.HOME_END_OF_LINE_STATION,"");
        }

        String jsonForWatch;

        if(result != null) {
            JsonArray trainPredictions = result.getAsJsonArray("Trains");

            JsonObject nextTrainJSON = null;

            for (int i = 0; i < trainPredictions.size(); i++) {
                JsonObject obj = trainPredictions.get(i).getAsJsonObject();
                if (obj.get("DestinationCode").getAsString().equals(endOfLineStation)) {
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
    }
}