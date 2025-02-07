package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Hello extends AppCompatActivity {
    Button registry, login, onmain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hello);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onmain = findViewById(R.id.onmain);
        onmain.setOnClickListener(v -> {
            Intent intent = new Intent(Hello.this, MainActivity.class);
            startActivity(intent);
        });
        registry = findViewById(R.id.registry);
        registry.setOnClickListener(v -> {
            Intent intent = new Intent(Hello.this, Registration.class);
            startActivity(intent);
        });
        login = findViewById(R.id.text);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(Hello.this, Login.class);
            startActivity(intent);
        });
    }

}