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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.longdo.mjpegviewer.MjpegView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//TODO: Need to add http request to send commands to the robot

public class ButtonControl extends AppCompatActivity {

    private boolean moving = false;

    //* MJPEG stream viewer object
    private MjpegView viewer;

    //* IP address of the robot
    private String ipAddress;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_control);

        //* Get IP address from intent
        ipAddress = getIntent().getStringExtra("IP_ADDRESS");

        Button btnUp = findViewById(R.id.forward);
        Button btnDown = findViewById(R.id.reverse);
        Button btnLeft = findViewById(R.id.left);
        Button btnRight = findViewById(R.id.right);
        Button btnStop = findViewById(R.id.stop);
        Button btnBack = findViewById(R.id.backBtn);

        //* Get MJPEG stream viewer object
        viewer = (MjpegView) findViewById(R.id.stream);

        TextView botIP = findViewById(R.id.robotIP);
        botIP.setText(String.format("IP: %s", ipAddress));

        //* Set MJPEG stream viewer settings
        viewer.setMode(MjpegView.MODE_FIT_WIDTH);
        viewer.setAdjustHeight(true);
        viewer.setSupportPinchZoomAndPan(true);
        viewer.setUrl("http://"+ipAddress+":81/stream");
        viewer.startStream();

        //* Set onTouchListener for forward button
        btnUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });

        //* Set onTouchListener for reverse button
        btnDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });

        //* Set onTouchListener for left button
        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });

        //* Set onTouchListener for right button
        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });

        //* Set onClickListener for stop button
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("stop");
                logMe("Stop");
                moving = false;
            }
        });

        //* Set onClickListener for back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewer.stopStream();
                finish();
            }
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
