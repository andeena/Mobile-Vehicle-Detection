package com.example.deteksigolongankendaraan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class SignUp extends AppCompatActivity {
    TextInputEditText name,email,password,confirmpassword;
    Button buttonSignUp;
    TextView textLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmpassword = findViewById(R.id.confirmpassword);

        buttonSignUp = findViewById(R.id.Buttonsignup);
        textLogin = findViewById(R.id.loginText);

        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dataName,dataPassword,dataEmail, dataConfirmPass;

                dataName = String.valueOf(name.getText());
                dataPassword = String.valueOf(password.getText());
                dataEmail = String.valueOf(email.getText());
                dataConfirmPass = String.valueOf(confirmpassword.getText());
                //Start ProgressBar first (Set visibility VISIBLE)
                if (!dataPassword.equals(dataConfirmPass)) {
                    Toast.makeText(getApplicationContext(),"Confirm Password is not same", Toast.LENGTH_SHORT).show();

                }
                if(!dataName.equals("") && !dataPassword.equals("") && !dataEmail.equals("") && !dataConfirmPass.equals("")) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Starting Write and Read data with URL
                            //Creating array for parameters
                            String[] field = new String[3]; // Sesuaikan jumlah field
                            field[0] = "name";
                            field[1] = "password";
                            field[2] = "email";

                            String[] data = new String[3];
                            data[0] = dataName;
                            data[1] = dataPassword;
                            data[2] = dataEmail;
                            PutData putData = new PutData("http://10.125.175.18/detection_vehicle/signup.php", "POST", field, data);
                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    String result = putData.getResult();
                                    if(result.equals("Sign Up Success")){
                                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("isSignedUp", true);
                                        editor.putString("userEmail", dataEmail);
                                        editor.apply();
                                        Intent intent =  new Intent(getApplicationContext(),Login.class);
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        finish();
                                    }  else{
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                    }
                                    //End ProgressBar (Set visibility to GONE)
                                    Log.i("PutData", result);
                                }
                            }
                            //End Write and Read data with URL
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


}