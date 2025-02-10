package com.mjt.dtcadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the card
        // Declare the card
        MaterialCardView addNotice = findViewById(R.id.addNotice);

        addNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Debugging logs
                System.out.println("addNotice clicked");
                Intent intent = new Intent(MainActivity.this,UploadNotice.class);
                startActivity(intent);
            }
        });

    }
}
