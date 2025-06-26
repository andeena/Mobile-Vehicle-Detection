package com.example.deteksigolongankendaraan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        ImageView imageViewVehicle = findViewById(R.id.imageViewDetect);
        TextView hasilGolongan = findViewById(R.id.hasilGolongan);
        TextView hargaGolongan = findViewById(R.id.hargaGolongan);
        MaterialButton buttonHome = findViewById(R.id.Buttonsignup);

        String golongan = getIntent().getStringExtra("GOLONGAN");
        String harga = getIntent().getStringExtra("HARGA");
        int iconRes = getIntent().getIntExtra("ICON_RES_ID", R.drawable.ic_suv);

        hasilGolongan.setText(golongan);
        hargaGolongan.setText("Harga: " + harga);
        imageViewVehicle.setImageResource(iconRes);

        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

