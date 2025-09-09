package com.example;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Get name from intent
        String userName = getIntent().getStringExtra("USER_NAME");

        // Show greeting
        TextView textView = findViewById(R.id.secondActivityText);
        textView.setText("Welcome, " + userName + "!");
    }
}
