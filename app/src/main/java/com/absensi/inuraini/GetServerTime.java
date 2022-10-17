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
    String ApiUrl = "hRnchtWYK9SYpNXQ9UmbvpVZtlGd/Umbvp3L05WZyJXdj9SZtlGVvkGch9ybp5SawFWZtlGduc3d39yL6MHc0RHa";
    RequestQueue requestQueue;

    public GetServerTime(Activity activity) {
        this.activity = activity;

        requestQueue = Volley.newRequestQueue(activity);
    }

    public void getDateTime(VolleyCallBack volleyCallBack){

        JSONObject jsonObject = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Preferences.retriveSec(ApiUrl), jsonObject, response -> {
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
