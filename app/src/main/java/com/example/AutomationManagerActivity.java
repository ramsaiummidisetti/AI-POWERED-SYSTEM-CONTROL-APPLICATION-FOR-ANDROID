package com.example;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai.SmartSuggestionManager;

import java.util.ArrayList;
import java.util.Set;

public class AutomationManagerActivity extends AppCompatActivity {

    private ArrayList<String> automationList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        setContentView(listView);

        loadAutomations();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                automationList
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {

            String selected = automationList.get(position);

            SmartSuggestionManager.removeAutomation(
                    this,
                    selected
            );

            automationList.remove(position);
            adapter.notifyDataSetChanged();

            Toast.makeText(
                    this,
                    "Removed: " + selected,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadAutomations() {

        Set<String> stored =
                SmartSuggestionManager.getActiveAutomations(this);

        automationList = new ArrayList<>(stored);
    }
}