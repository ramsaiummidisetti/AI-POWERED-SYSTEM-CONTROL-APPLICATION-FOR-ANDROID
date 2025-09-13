package com.example;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView textView = findViewById(R.id.textView);
        String userName = getIntent().getStringExtra("USER_NAME");

        if (userName != null) {
            textView.setText("Hello, " + userName + "!");
        }
    }
}
