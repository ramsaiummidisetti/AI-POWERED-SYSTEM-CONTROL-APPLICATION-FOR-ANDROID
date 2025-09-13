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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String t = titles.get(position);
        String d = details.get(position);
        holder.title.setText(t);
        holder.detail.setText(d);

        // Use built-in android icons to avoid missing drawables
        switch (t) {
            case "App Usage":
                holder.icon.setImageResource(android.R.drawable.ic_menu_agenda);
                break;
            case "Battery Info":
                holder.icon.setImageResource(android.R.drawable.ic_lock_idle_charging);
                break;
            case "Network":
                holder.icon.setImageResource(android.R.drawable.ic_menu_compass);
                break;
            case "Logs":
                holder.icon.setImageResource(android.R.drawable.ic_menu_info_details);
                break;
            default:
                holder.icon.setImageResource(android.R.drawable.ic_dialog_info);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t, position);
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, detail;
        ImageView icon;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.dashboard_item_title);
            detail = v.findViewById(R.id.dashboard_item_detail);
            icon = v.findViewById(R.id.cardIcon);
        }
    }
}
