package com.example.deteksigolongankendaraan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DashboardAdapter adapter;
    private List<DashboardItem> dashboardItems;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabScanCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initUI();
        setupRecyclerView();
        loadDashboardData(); // Memuat data yang baru
        setupListeners();
    }

    private void initUI() {
        recyclerView = findViewById(R.id.recyclerView_dashboard);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        fabScanCamera = findViewById(R.id.fab_scan_camera);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 kolom per baris
        dashboardItems = new ArrayList<>();
        adapter = new DashboardAdapter(this, dashboardItems);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        fabScanCamera.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
        });

        bottomAppBar.setNavigationOnClickListener(v -> {
            Toast.makeText(DashboardActivity.this, "Anda sudah di Home", Toast.LENGTH_SHORT).show();
        });

        bottomAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                Toast.makeText(DashboardActivity.this, "Fitur Setting akan datang!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    // --- METODE INI DIPERBARUI TOTAL ---
    private void loadDashboardData() {
        dashboardItems.clear(); // Bersihkan list sebelum menambahkan data baru

        // Menambahkan 12 golongan kendaraan baru
        // Angka terakhir adalah jumlah deteksi (data dummy)
        dashboardItems.add(new DashboardItem("GOL. I", R.drawable.ic_gol_1_bicycle, 10));
        dashboardItems.add(new DashboardItem("GOL. II", R.drawable.ic_gol_2_motorcycle, 153));
        dashboardItems.add(new DashboardItem("GOL. III", R.drawable.ic_gol_3_motorcycle_large, 25));
        dashboardItems.add(new DashboardItem("GOL. IVA", R.drawable.ic_gol_4a_passenger_car, 210));
        dashboardItems.add(new DashboardItem("GOL. IVB", R.drawable.ic_gol_4b_pickup, 45));
        dashboardItems.add(new DashboardItem("GOL. VA", R.drawable.ic_gol_5a_medium_bus, 18));
        dashboardItems.add(new DashboardItem("GOL. VB", R.drawable.ic_gol_5b_medium_truck, 33));
        dashboardItems.add(new DashboardItem("GOL. VIA", R.drawable.ic_gol_6a_large_bus, 22));
        dashboardItems.add(new DashboardItem("GOL. VII", R.drawable.ic_gol_7_tronton, 15));
        dashboardItems.add(new DashboardItem("GOL. VIII", R.drawable.ic_gol_8_tronton_large, 9));
        dashboardItems.add(new DashboardItem("GOL. IX", R.drawable.ic_gol_9_tronton_xl, 5));
        // Anda bisa menambahkan golongan lain di sini jika ada

        adapter.notifyDataSetChanged(); // Memberi tahu adapter bahwa data telah diperbarui
    }
}