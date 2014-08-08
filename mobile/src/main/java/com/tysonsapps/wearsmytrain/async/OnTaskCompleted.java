package com.tysonsapps.wearsmytrain.async;

import org.json.JSONObject;

/**
 * Created by jared on 8/5/14.
 */
public interface OnTaskCompleted{
    void onTaskCompleted(JSONObject result, String taskType);
}