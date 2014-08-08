package com.tysonsapps.wearsmytrain;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;

public class StationStatusActivity extends Activity implements MessageApi.MessageListener,GoogleApiClient.ConnectionCallbacks {

    private TextView mTimeTextView;
    private TextView mDestinationTextView;
    private LinearLayout mLayout;
    private GoogleApiClient mGoogleApiClient;
    private static final String START_ACTIVITY_PATH = "/fetch-train-times";
    private boolean mLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_status);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimeTextView = (TextView) stub.findViewById(R.id.time);
                mTimeTextView.setText("");

                mDestinationTextView = (TextView) stub.findViewById(R.id.destination);
                mDestinationTextView.setText("Loading...");

                mLayout = (LinearLayout) stub.findViewById(R.id.layout);

                mLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(mLoading == false){
                            mTimeTextView.setText("");
                            mDestinationTextView.setText("Loading...");
                            new RequestDataAsyncTask().execute();
                        }


                        return true;
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        String nextTrainJSONString = new String(messageEvent.getData());
        Log.d("WATCH Received:",new String(messageEvent.getData()));

        try {
            if(nextTrainJSONString.isEmpty()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDestinationTextView.setText("No trains...");
                        mTimeTextView.setText("");
                    }
                });
            }
            else{
                JSONObject nextTrain = new JSONObject(nextTrainJSONString);
                final String destination = nextTrain.getString("DestinationName");
                final String arrivalTime = nextTrain.getString("Min");
                final String color = nextTrain.getString("Line");


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTimeTextView.setText(arrivalTime);
                        mDestinationTextView.setText(destination);
                        colorize(color);
                    }
                });


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        mLoading = false;


    }

    private void colorize(String line){
        if(line.equals("GR")){
            mTimeTextView.setTextColor(getResources().getColor(android.R.color.white));
            mDestinationTextView.setTextColor(getResources().getColor(android.R.color.white));
            mLayout.setBackgroundColor(getResources().getColor(R.color.greenLine));
        }
        else if(line.equals("BL")){
            mTimeTextView.setTextColor(getResources().getColor(android.R.color.white));
            mDestinationTextView.setTextColor(getResources().getColor(android.R.color.white));
            mLayout.setBackgroundColor(getResources().getColor(R.color.blueLine));
        }
        else if(line.equals("SV")){
            mTimeTextView.setTextColor(getResources().getColor(android.R.color.black));
            mDestinationTextView.setTextColor(getResources().getColor(android.R.color.black));
            mLayout.setBackgroundColor(getResources().getColor(R.color.silverLine));
        }
        else if(line.equals("RD")){
            mTimeTextView.setTextColor(getResources().getColor(android.R.color.white));
            mDestinationTextView.setTextColor(getResources().getColor(android.R.color.white));
            mLayout.setBackgroundColor(getResources().getColor(R.color.redLine));
        }
        else if(line.equals("OR")){
            mTimeTextView.setTextColor(getResources().getColor(android.R.color.black));
            mDestinationTextView.setTextColor(getResources().getColor(android.R.color.black));
            mLayout.setBackgroundColor(getResources().getColor(R.color.orangeLine));
        }
        else if(line.equals("YL")){
            mTimeTextView.setTextColor(getResources().getColor(android.R.color.black));
            mDestinationTextView.setTextColor(getResources().getColor(android.R.color.black));
            mLayout.setBackgroundColor(getResources().getColor(R.color.yellowLine));
        }


    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);

        new RequestDataAsyncTask().execute();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public class RequestDataAsyncTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... args) {
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            if(nodes.getNodes().size() > 0){
                Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, nodes.getNodes().get(0).getId(), START_ACTIVITY_PATH, "".getBytes()).setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.d("WATCH OUTPUT", "Failed to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                } else {
                                    Log.d("WATCH OUTPUT", "Successfully requested train times");
                                }
                            }
                        }
                );
            }

            return null; //return value doesn't matter...
        }
    }
}
