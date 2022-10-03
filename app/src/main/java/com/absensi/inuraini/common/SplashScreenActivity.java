package com.absensi.inuraini.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.absensi.inuraini.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make call to execute AsycTasks<> here
        // This helps avoid the extra step of clicking on a button
        // to take you to the MainActivity
        new StartMainActivity().execute(this);

//        new Handler().postDelayed(this::finish, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private class StartMainActivity extends AsyncTask<Context, Void, Intent> {

        Context ctx;

        @Override
        protected Intent doInBackground(Context... params) {
            ctx = params[0];
            Intent intent = new Intent(ctx, LoginActivity.class);
            ctx.startActivity(intent);
            finish();
            return null;
        }
    }
}