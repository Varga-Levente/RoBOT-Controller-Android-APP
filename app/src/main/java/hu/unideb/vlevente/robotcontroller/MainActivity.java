package hu.unideb.vlevente.robotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCtrl = findViewById(R.id.btnCtrl);
        Button rotCtrl = findViewById(R.id.rotCtrl);
        btnCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openButtonControl();
            }
        });
    }

    public void openButtonControl(){
        Intent intent = new Intent(this, ButtonControl.class);
        startActivity(intent);
    }
}