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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ButtonControl extends AppCompatActivity {

    private boolean moving = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_control);

        Button btnUp = findViewById(R.id.forward);
        Button btnDown = findViewById(R.id.reverse);
        Button btnLeft = findViewById(R.id.left);
        Button btnRight = findViewById(R.id.right);
        Button btnStop = findViewById(R.id.stop);

        // Set onTouchListener for buttons
        btnUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("Move: Forward");
                    logMe("Forward");
                    moving = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("Move: Stop");
                    logMe("Stop");
                    moving = false;
                }
                return true;
            }
        });

        btnDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("Move: Reverse");
                    logMe("Reverse");
                    moving = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("Move: Stop");
                    logMe("Stop");
                    moving = false;
                }
                return true;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("Move: Left");
                    logMe("Left");
                    moving = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("Move: Stop");
                    logMe("Stop");
                    moving = false;
                }
                return true;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    System.out.println("Move: Right");
                    logMe("Right");
                    moving = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    System.out.println("Move: Stop");
                    logMe("Stop");
                    moving = false;
                }
                return true;
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Move: Stop");
                logMe("Stop");
                moving = false;
            }
        });

    }

    //Create a function to add new text to log
    public void logMe(String text) {
        //Get time in hh:mm:ss format
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        LinearLayout log = findViewById(R.id.scrolllinearlayout);
        TextView tv = new TextView(this);
        //If text is Stop, set color to red
        if(text.equals("Stop")) {
            tv.setTextColor(Color.RED);
        }else{
            tv.setTextColor(Color.GREEN);
        }
        tv.setText("["+time+"] - "+text);
        log.addView(tv);
    }
}
