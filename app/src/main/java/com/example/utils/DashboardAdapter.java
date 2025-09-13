package com.example.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private final List<String> titles;
    private final List<String> details;
    private final OnItemClickListener listener;

    // Click listener interface
    public interface OnItemClickListener {
        void onItemClick(String itemTitle);
    }

    // Constructor
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
        holder.details.setText(detail);

        // Set icons dynamically based on title
        switch (title) {
            case "App Usage":
                holder.icon.setImageResource(android.R.drawable.ic_menu_manage);
                break;
            case "Battery Info":
                holder.icon.setImageResource(android.R.drawable.ic_lock_idle_charging);
                break;
            case "Network":
                holder.icon.setImageResource(android.R.drawable.presence_online);
                break;
            case "Logs":
                holder.icon.setImageResource(android.R.drawable.ic_menu_agenda);
                break;
            default:
                holder.icon.setImageResource(android.R.drawable.ic_menu_info_details); // fallback
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(title));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    // ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, details;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dashboard_item_title);
            details = itemView.findViewById(R.id.dashboard_item_detail);
            icon = itemView.findViewById(R.id.dashboard_item_icon);
        }
    }
}
