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
import java.util.Objects;

//TODO: Need to add http request to send commands to the robot

public class RotationControl extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView forwardTextView, backwardTextView, leftTextView, rightTextView, EH, JB, botIP, version, devModeText, connText;
    private Button btnBack;

    private String ipAddress;

    //* MJPEG stream viewer object
    private MjpegView viewer;

    //* Last direction of the robot
    private String lastDirection = "stop";

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
        version = findViewById(R.id.version);
        devModeText = findViewById(R.id.devmodetext);
        connText = findViewById(R.id.connectingtext);

        //* Hide dev mode text if DEV_MODE is false
        if(!getIntent().getBooleanExtra("DEV_MODE", true)){
            devModeText.setVisibility(View.GONE);
        }else {
            connText.setVisibility(View.GONE);
        }

        //* Get MJPEG stream viewer object
        viewer = (MjpegView) findViewById(R.id.stream);

        //* Set MJPEG stream viewer settings
        viewer.setMode(MjpegView.MODE_FIT_WIDTH);
        viewer.setAdjustHeight(true);
        viewer.setSupportPinchZoomAndPan(true);
        viewer.setUrl("http://"+ipAddress+":81/stream");
        viewer.startStream();

        //* Set current version from main activity
        version.setText(version.getText().toString().replace("#.#", Objects.requireNonNull(getIntent().getStringExtra("VERSION"))));

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

        //* Start connection animation
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean finished = false;
                while (!isFinishing()) {
                    try {
                        for(int i=0; i<15; i++) {
                            if (finished) {
                                break;
                            }
                            Thread.sleep(500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (connText.getText().toString().endsWith("...")) {
                                        connText.setText("Connecting");
                                    } else {
                                        connText.setText(connText.getText().toString() + ".");
                                    }
                                }
                            });
                        }
                        finished = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connText.setText("Can't connect to camera");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
            float accelerometerY = event.values[1];
            float accelerometerZ = event.values[2];

            String currentDirection = "stop";
            String forwardText = "⛔";
            String backwardText = "⛔";
            String leftText = "⛔";
            String rightText = "⛔";

            if (accelerometerZ > 7.8) {
                currentDirection = "forward";
                forwardText = "⬆️";
            } else if (accelerometerZ < 2.5) {
                currentDirection = "backward";
                backwardText = "⬇️";
            } else if (accelerometerY < -3.5) {
                currentDirection = "left";
                leftText = "⬅️";
            } else if (accelerometerY >= 3.5) {
                currentDirection = "right";
                rightText = "➡️";
            }

            if (!currentDirection.equals(lastDirection)) {
                move(currentDirection);
                lastDirection = currentDirection;
            }

            forwardTextView.setText(forwardText);
            backwardTextView.setText(backwardText);
            leftTextView.setText(leftText);
            rightTextView.setText(rightText);

            DecimalFormat df = new DecimalFormat("#.##");
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
        System.out.println("[Rotation] Move: " + direction);
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