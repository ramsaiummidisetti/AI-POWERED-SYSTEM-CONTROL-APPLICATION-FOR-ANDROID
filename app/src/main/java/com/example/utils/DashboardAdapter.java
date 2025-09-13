package com.example.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private final List<String> titles;
    private final List<String> details;
    private final OnItemClickListener listener;

    // Constructor now takes titles, details, and listener
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

        holder.itemTitle.setText(title);
        holder.itemDetail.setText(detail);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(title));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;
        TextView itemDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.dashboard_item_title);
            itemDetail = itemView.findViewById(R.id.dashboard_item_detail);
        }
    }

    // Click listener interface
    public interface OnItemClickListener {
        void onItemClick(String item);
    }
}
