package com.example;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Retrieve passed data
        String userName = getIntent().getStringExtra("USER_NAME");
        TextView textView = findViewById(R.id.textView);
        textView.setText(userName != null && !userName.isEmpty() ? "Hello, " + userName : "Welcome to Second Activity");
    }
}