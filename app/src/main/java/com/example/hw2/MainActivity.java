package com.example.hw2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button start, reset, lap, switch_view;
    TextView timeView;
    MyAsyncTask myAsyncTask;
    int seconds = 0;
    private LapFragment lapFragment;
    String prev;
    String times;
    boolean landscape = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            landscape = true;

        // initialize views
        lapFragment = new LapFragment();
        start = findViewById(R.id.start_but);
        reset = findViewById(R.id.reset_but);
        lap = findViewById(R.id.lap_but);
        timeView = findViewById(R.id.timer);
        myAsyncTask = new MyAsyncTask();

        // only create switch view button if we are in portrait
        if (!landscape) {
            switch_view = findViewById(R.id.switch_view);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, lapFragment)
                    .hide(lapFragment)
                    .commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, lapFragment)
                    .commit();
        }

        // on click listener for our buttons
        View.OnClickListener ClickListener = v -> {
            // start button
            if (v.getId() == start.getId()) {
                // first call to start
                if (myAsyncTask.getStatus() != AsyncTask.Status.RUNNING && seconds == 0) {
                    times = "";
                    myAsyncTask = new MyAsyncTask();
                    myAsyncTask.execute();
                    start.setText(R.string.stop);
                    start.setBackgroundColor(getResources().getColor(R.color.stop_col));
                }
                // start from pause
                else if (myAsyncTask.getStatus() != AsyncTask.Status.RUNNING && seconds != 0){
                    myAsyncTask = new MyAsyncTask();
                    myAsyncTask.execute();
                    start.setText(R.string.stop);
                    start.setBackgroundColor(getResources().getColor(R.color.stop_col));
                }
                // pause
                else {
                    myAsyncTask.cancel(true);
                    start.setText(R.string.start);
                    start.setBackgroundColor(getResources().getColor(R.color.start_col));
                }
            }
            // reset button
            else if (v.getId() == reset.getId()) {
                myAsyncTask.cancel(true);
                timeView.setText(R.string.timer_place_holder);
                start.setText(R.string.start);
                start.setBackgroundColor(getResources().getColor(R.color.start_col));
                seconds = 0;
                times = null;
                lapFragment.updateTextView("");
            }
            // lap button
            else if (myAsyncTask.getStatus() == AsyncTask.Status.RUNNING && v.getId() == lap.getId()) {
                Context context = getApplicationContext();
                CharSequence text = timeView.getText();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                // Don't lap if previous lap is from same second
                if (prev == null) {
                    times += timeView.getText().toString() + "\n";
                    toast.show();
                }
                else if (!prev.equals(timeView.getText().toString())) {
                    toast.show();
                    times += timeView.getText().toString() + "\n";
                }
                lapFragment.updateTextView(times);
                prev = timeView.getText().toString();
            }
            // switch views (portrait only)
            else if (!landscape && v.getId() == switch_view.getId()) {
                if (lapFragment.isVisible()) {
                    switch_view.setText(R.string.lap_times);
                    getSupportFragmentManager().beginTransaction()
                            .hide(lapFragment)
                            .commit();
                }
                else {
                    switch_view.setText(R.string.back);
                    getSupportFragmentManager().beginTransaction()
                            .show(lapFragment)
                            .commit();
                }
            }
        };

        // listeners
        start.setOnClickListener(ClickListener);
        reset.setOnClickListener(ClickListener);
        lap.setOnClickListener(ClickListener);
        if (!landscape) switch_view.setOnClickListener(ClickListener);

        // get saved fields for orientation change
        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds");
            prev = savedInstanceState.getString("prev");
            times = savedInstanceState.getString("times");
            timeView.setText(savedInstanceState.getString("time"));
            if (savedInstanceState.getBoolean("running")) {
                myAsyncTask.cancel(true);
                start.performClick();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (myAsyncTask != null && myAsyncTask.getStatus() != AsyncTask.Status.RUNNING) {
            myAsyncTask.cancel(true);
            myAsyncTask = null;
        }
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            while (true) {
                try {
                    if (isCancelled()) break;
                    lapFragment.updateTextView(times);
                    Thread.sleep(1000);
                    seconds++;
                    int hours = seconds / 3600;
                    int minutes = (seconds - (hours * 60)) / 60;
                    int secs = seconds - (hours * 3600) - (minutes * 60);
                    // create time string
                    @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                    publishProgress(time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onProgressUpdate(String... text) {
            super.onProgressUpdate(text);
            timeView.setText(text[0]);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("prev", prev);
        outState.putString("times", times);
        outState.putString("time", timeView.getText().toString());
        outState.putInt("seconds", seconds);
        outState.putBoolean("running", myAsyncTask.getStatus() == AsyncTask.Status.RUNNING);
        myAsyncTask.cancel(true);
    }
}
