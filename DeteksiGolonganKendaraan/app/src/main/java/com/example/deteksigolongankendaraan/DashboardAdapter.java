package com.example.deteksigolongankendaraan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private List<DashboardItem> itemList;
    private Context context;

    public DashboardAdapter(Context context, List<DashboardItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardItem item = itemList.get(position);

        holder.vehicleName.setText(item.getVehicleName());
        holder.vehicleIcon.setImageResource(item.getVehicleIconResId());
        holder.vehicleCount.setText(String.format(Locale.getDefault(), "%d Terdeteksi", item.getDetectionCount()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleIcon;
        TextView vehicleName;
        TextView vehicleCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleIcon = itemView.findViewById(R.id.imageView_vehicle_icon);
            vehicleName = itemView.findViewById(R.id.textView_vehicle_name);
            vehicleCount = itemView.findViewById(R.id.textView_vehicle_count);
        }
    }
}