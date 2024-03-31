package hu.unideb.vlevente.robotcontroller;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.longdo.mjpegviewer.MjpegView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//TODO: Need to add http request to send commands to the robot

public class RotationControl extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView forwardTextView;
    private TextView backwardTextView;
    private TextView leftTextView;
    private TextView rightTextView;
    private TextView EH;
    private TextView JB;
    private TextView botIP;
    private Button btnBack;

    private String ipAddress;

    //* MJPEG stream viewer object
    private MjpegView viewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_control);

        //* Get IP address from intent
        ipAddress = getIntent().getStringExtra("IP_ADDRESS");

        forwardTextView = findViewById(R.id.forwardTextView);
        backwardTextView = findViewById(R.id.backwardTextView);
        leftTextView = findViewById(R.id.leftTextView);
        rightTextView = findViewById(R.id.rightTextView);
        EH = findViewById(R.id.EH);
        JB = findViewById(R.id.JB);
        botIP = findViewById(R.id.robotIP);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        btnBack = findViewById(R.id.backBtn);

        //* Get MJPEG stream viewer object
        viewer = (MjpegView) findViewById(R.id.stream);

        //* Set MJPEG stream viewer settings
        viewer.setMode(MjpegView.MODE_FIT_WIDTH);
        viewer.setAdjustHeight(true);
        viewer.setSupportPinchZoomAndPan(true);
        viewer.setUrl("http://"+ipAddress+":81/stream");
        viewer.startStream();

        //* Handle back button on press (finish activity)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewer.stopStream();
                finish();
            }
        });

        botIP.setText("IP: "+ipAddress);
    }

    //* Register sensor listener on resume
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //* Register sensor listener on pause
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //* Implement sensor listener methods (Currently only accelerometer is used)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    //* Handle sensor changes
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //* Get accelerometer values
            float accelerometerY = event.values[1];
            float accelerometerZ = event.values[2];

            //* Set negative threshold status
            String negativeThresholdSttaus = "⛔";

            //* Set text views based on accelerometer values
                //! this.forwardTextView.setText(accelerometerZ > 7.8 ? "⬆️" : negativeThresholdSttaus);
            if(accelerometerZ > 7.8) {
                this.forwardTextView.setText("⬆️");
                move("forward");
            } else {
                this.forwardTextView.setText(negativeThresholdSttaus);
                move("stop");
            }

                //! this.backwardTextView.setText(accelerometerZ < 2.8 ? "⬇️" : negativeThresholdSttaus);
            if(accelerometerZ < 2.5) {
                this.backwardTextView.setText("⬇️");
                move("backward");
            } else {
                this.backwardTextView.setText(negativeThresholdSttaus);
                move("stop");
            }

                //! this.leftTextView.setText(accelerometerY < -3.5 ? "⬅️" : negativeThresholdSttaus);
            if(accelerometerY < -3.5) {
                this.leftTextView.setText("⬅️");
                move("left");
            } else {
                this.leftTextView.setText(negativeThresholdSttaus);
                move("stop");
            }

                //! this.rightTextView.setText(accelerometerY >= 3.5 ? "➡️" : negativeThresholdSttaus);
            if(accelerometerY >= 3.5) {
                this.rightTextView.setText("➡️");
                move("right");
            } else {
                this.rightTextView.setText(negativeThresholdSttaus);
                move("stop");
            }

            //* Format accelerometer values
            DecimalFormat df = new DecimalFormat("#.##");

            //* Set accelerometer values text views (EH and JB)
            this.EH.setText("EH: " + df.format((double) accelerometerZ));
            this.JB.setText("JB: " + df.format((double) accelerometerY));
        }
    }

    public void logMe(String text) {
        //* Get time in hh:mm:ss format
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        LinearLayout log = findViewById(R.id.scrolllinearlayout);
        TextView tv = new TextView(this);
        //* Set text color based on the content
        if(text.equals("Stop")) {
            //* If text is "Stop", set color to red
            tv.setTextColor(Color.RED);
        }else if(text.contains("BOT")) {
            //* If text contains "BOT", set color to blue
            tv.setTextColor(Color.BLUE);
        }else{
            //* If text is not "Stop" and does not contain "BOT", set color to green
            tv.setTextColor(Color.GREEN);
        }
        tv.setText(String.format("[%s] - %s", time, text));
        log.addView(tv);

        if(text.contains("BOT")){
            TextView separator = new TextView(this);
            separator.setText("----------------------");
            log.addView(separator);
        }
    }

    private void move(String direction) {
        String url = "http://" + ipAddress + "/action?go=" + direction;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Ha a válasz tartalmazza az "OK" szöveget, akkor sikeres
                        if (response.contains("OK")) {
                            logMe("[BOT] - OK");
                        } else {
                            logMe("[BOT] - Fail");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hibakezelés
                        logMe("[BOT] - Fail: " + error.getMessage());
                    }
                });

        // Kérelem hozzáadása a kérés sorhoz
        requestQueue.add(stringRequest);
    }
}