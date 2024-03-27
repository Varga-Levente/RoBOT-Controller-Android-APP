package hu.unideb.vlevente.robotcontroller;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class RotationControl extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView forwardTextView;
    private TextView backwardTextView;
    private TextView leftTextView;
    private TextView rightTextView;
    private TextView EH;
    private TextView JB;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_control);

        forwardTextView = findViewById(R.id.forwardTextView);
        backwardTextView = findViewById(R.id.backwardTextView);
        leftTextView = findViewById(R.id.leftTextView);
        rightTextView = findViewById(R.id.rightTextView);
        EH = findViewById(R.id.EH);
        JB = findViewById(R.id.JB);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        btnBack = findViewById(R.id.backBtn);

        //* Handle back button on press (finish activity)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
            this.forwardTextView.setText(accelerometerZ > 7.8 ? "⬆️" : negativeThresholdSttaus);

            this.backwardTextView.setText(accelerometerZ < 2.5 ? "⬇️" : negativeThresholdSttaus);

            this.leftTextView.setText(accelerometerY < -3.5 ? "⬅️" : negativeThresholdSttaus);

            this.rightTextView.setText(accelerometerY >= 3.5 ? "➡️" : negativeThresholdSttaus);

            //* Format accelerometer values
            DecimalFormat df = new DecimalFormat("#.##");

            //* Set accelerometer values text views (EH and JB)
            this.EH.setText("EH: " + df.format((double) accelerometerZ));
            this.JB.setText("JB: " + df.format((double) accelerometerY));
        }
    }
}