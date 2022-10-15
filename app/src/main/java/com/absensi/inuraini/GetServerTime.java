package com.absensi.inuraini;

import android.app.Activity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class GetServerTime {

    Activity activity;
    String ApiUrl = "https://www.timeapi.io/api/Time/current/zone?timeZone=Asia/Jakarta";
    RequestQueue requestQueue;

    public GetServerTime(Activity activity) {
        this.activity = activity;

        requestQueue = Volley.newRequestQueue(activity);
    }

    public void getDateTime(VolleyCallBack volleyCallBack){

        JSONObject jsonObject = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiUrl, jsonObject, response -> {
            try {
                volleyCallBack.onGetDateTime(response.getString("date"), response.getString("time"));
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {

        });

        requestQueue.add(request);
    }

    public interface VolleyCallBack{
        void onGetDateTime(String date, String time);
    }
}
