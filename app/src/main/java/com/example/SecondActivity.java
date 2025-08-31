// package com.example;
// import androidx.appcompat.app.AppCompatActivity;
// import android.os.Bundle;
// import android.widget.TextView;
// public class SecondActivity extends AppCompatActivity {
//     private TextView greetingTextView;
//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         setContentView(R.layout.activity_second);
//         greetingTextView = findViewById(R.id.greetingTextView);
//         // Get the Intent that started this activity
//         Bundle extras = getIntent().getExtras();
//         if (extras != null) {
//             String userName = extras.getString("USER_NAME");
//             if (userName != null && !userName.isEmpty()) {
//             greetingTextView.setText("Hello, " + userName + "!");
//             }
//         }
// }
// }
package com.example;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        String userInput = getIntent().getStringExtra("USER_NAME");
        TextView userNameText = findViewById(R.id.userNameText);
        userNameText.setText(userInput != null ? userInput : "");
    }
}