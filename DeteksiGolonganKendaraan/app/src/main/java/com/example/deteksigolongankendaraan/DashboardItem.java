package com.example.deteksigolongankendaraan;

public class DashboardItem {
    private String vehicleName;
    private int vehicleIconResId;
    private int detectionCount;

    public DashboardItem(String vehicleName, int vehicleIconResId, int detectionCount) {
        this.vehicleName = vehicleName;
        this.vehicleIconResId = vehicleIconResId;
        this.detectionCount = detectionCount;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public int getVehicleIconResId() {
        return vehicleIconResId;
    }

    public int getDetectionCount() {
        return detectionCount;
    }

    // Setter jika Anda ingin mengubah count nanti
    public void setDetectionCount(int detectionCount) {
        this.detectionCount = detectionCount;
    }
}