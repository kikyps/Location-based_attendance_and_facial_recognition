package com.absensi.inuraini;

import android.view.View;
import android.view.ViewConfiguration;

public abstract class OnDoubleClickListener implements View.OnClickListener {

    private static final int TIME_OUT = ViewConfiguration.getDoubleTapTimeout();
    private final TapHandler tapHandler = new TapHandler();

    public abstract void onSingleClick(View v);
    public abstract void onDoubleClick(View v);

    @Override
    public void onClick(View v) {
        tapHandler.cancelSingleTap(v);
        if (tapHandler.isDoubleTap()){
            onDoubleClick(v);
        } else {
            tapHandler.performSingleTap(v);
        }
    }

    private class TapHandler implements Runnable {
        public boolean isDoubleTap() {
            final long tapTime = System.currentTimeMillis();
            boolean doubleTap = tapTime - lastTapTime < TIME_OUT;
            lastTapTime = tapTime;
            return doubleTap;
        }
        public void performSingleTap(View v) {
            view = v;
            v.postDelayed(this, TIME_OUT);
        }
        public void cancelSingleTap(View v) {
            view = null;
            v.removeCallbacks(this);
        }

        @Override
        public void run() {
            if (view != null) {
                onSingleClick(view);
            }
        }
        private View view;
        private long lastTapTime = 0;
    }
}
