package com.absensi.inuraini.admin.location;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.absensi.inuraini.R;
import com.absensi.inuraini.spotlight.OnSpotlightStateChangedListener;
import com.absensi.inuraini.spotlight.OnTargetStateChangedListener;
import com.absensi.inuraini.spotlight.Spotlight;
import com.absensi.inuraini.spotlight.shape.Circle;
import com.absensi.inuraini.spotlight.target.CustomTarget;
import com.absensi.inuraini.spotlight.target.SimpleTarget;
import com.absensi.inuraini.spotlight.target.Target;

import java.util.ArrayList;

public class SpotTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_test);
        findViewById(R.id.simple_target).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View one = findViewById(R.id.one);
                int[] oneLocation = new int[2];
                one.getLocationInWindow(oneLocation);
                float oneX = oneLocation[0] + one.getWidth() / 2f;
                float oneY = oneLocation[1] + one.getHeight() / 2f;
                // make an target
                SimpleTarget firstTarget = new SimpleTarget.Builder(SpotTestActivity.this).setPoint(oneX, oneY)
                        .setShape(new Circle(100f))
                        .setTitle("first title")
                        .setDescription("first description")
                        .build();

                View two = findViewById(R.id.two);
                int[] twoLocation = new int[2];
                two.getLocationInWindow(twoLocation);
                PointF point =
                        new PointF(twoLocation[0] + two.getWidth() / 2f, twoLocation[1] + two.getHeight() / 2f);
                // make an target
                SimpleTarget secondTarget = new SimpleTarget.Builder(SpotTestActivity.this).setPoint(point)
                        .setShape(new Circle(80f))
                        .setTitle("second title")
                        .setDescription("second description")
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                Toast.makeText(SpotTestActivity.this, "target is started", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                Toast.makeText(SpotTestActivity.this, "target is ended", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build();

                SimpleTarget thirdTarget;

                thirdTarget = new SimpleTarget.Builder(SpotTestActivity.this).setPoint(findViewById(R.id.three))
                        .setShape(new Circle(200f))
                        .setTitle("third title")
                        .setDescription("third description")
                        .build();

                Spotlight.with(SpotTestActivity.this)
                        .setOverlayColor(R.color.background)
                        .setDuration(100L)
                        .setAnimation(new DecelerateInterpolator(2f))
                        .setTargets(firstTarget, secondTarget, thirdTarget)
                        .setClosedOnTouchedOutside(true)
                        .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                            @Override
                            public void onStarted() {
                                Toast.makeText(SpotTestActivity.this, "spotlight is started", Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onEnded() {
                                Toast.makeText(SpotTestActivity.this, "spotlight is ended", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .start();
            }
        });

        findViewById(R.id.custom_target).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater inflater = LayoutInflater.from(SpotTestActivity.this);

                ArrayList<Target> targets = new ArrayList<>();

                // make an target
                View first = inflater.inflate(R.layout.layout_target, null);
                final CustomTarget firstTarget =
                        new CustomTarget.Builder(SpotTestActivity.this).setPoint(findViewById(R.id.one))
                                .setShape(new Circle(100f))
                                .setOverlay(first)
                                .build();

                targets.add(firstTarget);

                View second = inflater.inflate(R.layout.layout_target, null);
                final CustomTarget secondTarget =
                        new CustomTarget.Builder(SpotTestActivity.this).setPoint(findViewById(R.id.two))
                                .setShape(new Circle(800f))
                                .setOverlay(second)
                                .build();

                targets.add(secondTarget);

                View third = inflater.inflate(R.layout.layout_target, null);
                final CustomTarget thirdTarget =
                        new CustomTarget.Builder(SpotTestActivity.this).setPoint(findViewById(R.id.three))
                                .setShape(new Circle(200f))
                                .setOverlay(third)
                                .build();

                targets.add(thirdTarget);

                final Spotlight spotlight =

                        Spotlight.with(SpotTestActivity.this)
                                .setOverlayColor(R.color.background)
                                .setDuration(1000L)
                                .setAnimation(new DecelerateInterpolator(2f))
                                .setTargets(targets)
                                .setClosedOnTouchedOutside(false)
                                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                                    @Override
                                    public void onStarted() {
                                        Toast.makeText(SpotTestActivity.this, "spotlight is started", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    @Override
                                    public void onEnded() {
                                        Toast.makeText(SpotTestActivity.this, "spotlight is ended", Toast.LENGTH_SHORT).show();
                                    }
                                });
                spotlight.start();

                View.OnClickListener closeTarget = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spotlight.closeCurrentTarget();
                    }
                };

                View.OnClickListener closeSpotlight = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spotlight.closeSpotlight();
                    }
                };

                first.findViewById(R.id.close_target).setOnClickListener(closeTarget);
                second.findViewById(R.id.close_target).setOnClickListener(closeTarget);
                third.findViewById(R.id.close_target).setOnClickListener(closeTarget);

                first.findViewById(R.id.close_spotlight).setOnClickListener(closeSpotlight);
                second.findViewById(R.id.close_spotlight).setOnClickListener(closeSpotlight);
                third.findViewById(R.id.close_spotlight).setOnClickListener(closeSpotlight);
            }
        });
    }
}