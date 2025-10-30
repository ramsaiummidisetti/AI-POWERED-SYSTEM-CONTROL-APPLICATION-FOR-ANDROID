package com.example.utils;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String title, int position);
    }

    private final List<String> titles;
    private final List<String> details;
    private final OnItemClickListener listener;

    public DashboardAdapter(List<String> titles, List<String> details, OnItemClickListener listener) {
        this.titles = titles;
        this.details = details;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = titles.get(position);
        String detail = details.get(position);

        holder.title.setText(title);
        holder.detail.setText(detail);

        // 🌈 Assign appropriate icon for each card
        switch (title) {
            case "App Usage":
                holder.icon.setImageResource(R.drawable.ic_app_usage);
                break;
            case "Battery Info":
                holder.icon.setImageResource(R.drawable.ic_battery);
                break;
            case "Network":
                holder.icon.setImageResource(R.drawable.ic_network);
                break;
            case "Bluetooth":
                holder.icon.setImageResource(R.drawable.ic_bluetooth);
                break;
            case "NFC":
                holder.icon.setImageResource(R.drawable.ic_nfc);
                break;
            default:
                holder.icon.setImageResource(android.R.drawable.ic_dialog_info);
                break;
        }

        // 💡 Dynamic color feedback for quick visual cues
        if (detail.toLowerCase().contains("off") || detail.toLowerCase().contains("not")) {
            holder.detail.setTextColor(Color.parseColor("#E53935")); // red for off
        } else if (detail.toLowerCase().contains("on") || detail.toLowerCase().contains("charging")) {
            holder.detail.setTextColor(Color.parseColor("#43A047")); // green for on
        } else {
            holder.detail.setTextColor(Color.parseColor("#0288D1")); // blue for neutral
        }

        // 🌙 Card elevation and ripple on touch
        holder.cardView.setCardElevation(6f);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(title, position);
            holder.cardView.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100)
                    .withEndAction(() -> holder.cardView.animate().scaleX(1f).scaleY(1f).setDuration(100));
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, detail;
        ImageView icon;
        CardView cardView;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.dashboard_item_title);
            detail = v.findViewById(R.id.dashboard_item_detail);
            icon = v.findViewById(R.id.cardIcon); // ✅ Correct ID from XML
            cardView = v.findViewById(R.id.dashboard_item_card);
        }
    }
}
