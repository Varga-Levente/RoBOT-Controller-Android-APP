package hu.unideb.vlevente.robotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.longdo.mjpegviewer.MjpegView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

//TODO: Need to add http request to send commands to the robot

public class ButtonControl extends AppCompatActivity {

    Button btnUp, btnDown, btnLeft, btnRight, btnStop, btnBack;
    TextView botIP, version, devModeText, connText;

    boolean moving = false;

    //* MJPEG stream viewer object
    private MjpegView viewer;

    //* IP address of the robot
    private String ipAddress;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_control);

        //* Get extras from intent
        ipAddress = getIntent().getStringExtra("IP_ADDRESS");

        btnUp = findViewById(R.id.forward);
        btnDown = findViewById(R.id.reverse);
        btnLeft = findViewById(R.id.left);
        btnRight = findViewById(R.id.right);
        btnStop = findViewById(R.id.stop);
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
        viewer = findViewById(R.id.stream);

        //* Set robot IP address text
        botIP = findViewById(R.id.robotIP);
        botIP.setText(String.format("IP: %s", ipAddress));

        //* Set current version from main activity
        version.setText(version.getText().toString().replace("#.#", Objects.requireNonNull(getIntent().getStringExtra("VERSION"))));

        //* Set MJPEG stream viewer settings
        viewer.setMode(MjpegView.MODE_FIT_WIDTH);
        viewer.setAdjustHeight(true);
        viewer.setSupportPinchZoomAndPan(true);
        viewer.setUrl("http://"+ipAddress+":81/stream");
        viewer.startStream();

        //* Set onTouchListener for forward button
        btnUp.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                move("forward");
                logMe("Forward");
                moving = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                move("stop");
                logMe("Stop");
                moving = false;
            }
            return true;
        });

        //* Set onTouchListener for reverse button
        btnDown.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                move("backward");
                logMe("Reverse");
                moving = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                move("stop");
                logMe("Stop");
                moving = false;
            }
            return true;
        });

        //* Set onTouchListener for left button
        btnLeft.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                move("left");
                logMe("Left");
                moving = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                move("stop");
                logMe("Stop");
                moving = false;
            }
            return true;
        });

        //* Set onTouchListener for right button
        btnRight.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                move("right");
                logMe("Right");
                moving = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                move("stop");
                logMe("Stop");
                moving = false;
            }
            return true;
        });

        //* Set onClickListener for stop button
        btnStop.setOnClickListener(v -> {
            move("stop");
            logMe("Stop");
            moving = false;
        });

        //* Set onClickListener for back button
        btnBack.setOnClickListener(v -> {
            viewer.stopStream();
            finish();
        });

    }

    //* Create a function to add new text to log
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
                response -> {
                    // Ha a válasz tartalmazza az "OK" szöveget, akkor sikeres
                    if (response.contains("OK")) {
                        logMe("[BOT] - OK");
                    } else {
                        logMe("[BOT] - Fail");
                    }
                },
                error -> {
                    // Hibakezelés
                    logMe("[BOT] - Fail: " + error.getMessage());
                });

        // Kérelem hozzáadása a kérés sorhoz
        requestQueue.add(stringRequest);
    }

    //* Animate connecting text
    /*@SuppressLint({"SetTextI18n", "ThreadSleepDuringRun"})
    @Override
    protected void onResume() {
        super.onResume();
        new Thread(() -> {
            boolean finished = false;
            while (!isFinishing()) {
                try {
                    for(int i=0; i<15; i++) {
                        if (finished) {
                            break;
                        }
                        Thread.sleep(500);
                        runOnUiThread(() -> {
                            if (connText.getText().toString().endsWith("...")) {
                                connText.setText("Connecting");
                            } else {
                                connText.setText(connText.getText().toString() + ".");
                            }
                        });
                    }
                    finished = true;
                    runOnUiThread(() -> connText.setText("Can't connect to camera"));
                } catch (InterruptedException e) {
                    System.out.println("ERROR: "+e.getMessage());
                }
            }
        }).start();
    }*/

}
