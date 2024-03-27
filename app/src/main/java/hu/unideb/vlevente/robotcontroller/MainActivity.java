package hu.unideb.vlevente.robotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button btnCtrl;
    Button rotCtrl;
    Button gitUrlBtn;
    EditText ipInput;

    //! This checkbox is for development purposes only and should be removed in the final version
    CheckBox devMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCtrl = findViewById(R.id.btnCtrl);
        rotCtrl = findViewById(R.id.rotCtrl);
        gitUrlBtn = findViewById(R.id.gitUrl);
        ipInput = findViewById(R.id.botIP);

        //! This checkbox is for development purposes only and should be removed in the final version
        devMode = findViewById(R.id.devmode);

        //* This method handles the "button control" button press
        btnCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateIP(ipInput.getText().toString())) {
                    ipInput.setError("Invalid IP address");
                    return;
                }
                openButtonControl();
            }
        });

        //* This method handles the "rotation control" button press
        rotCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateIP(ipInput.getText().toString())) {
                    ipInput.setError("Invalid IP address");
                    return;
                }
                openRotationControl();
            }
        });

        //* This method opens the github repository of the project on pressing the button
        gitUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://github.com/Varga-Levente/RoBOT"));
                startActivity(intent);
            }
        });

    }

    //* This method opens the button control activity
    public void openButtonControl(){
        Intent intent = new Intent(this, ButtonControl.class);
        startActivity(intent);
    }

    //* This method opens the rotation control activity
    public void openRotationControl(){
        Intent intent = new Intent(this, RotationControl.class);
        startActivity(intent);
    }

    //* This method validates the IP address and checks if the devmode checkbox is checked
    public boolean validateIP (String IP){
        //* Validate private ip address using regex if valid return true or devmode is checked
        //! In the final version the devmode checkbox should be removed
        if (devMode.isChecked()) return true;
        return IP.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
    }
}