package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainGlobal extends AppCompatActivity {
    Button volunteer_task, zak_task, confirm_task;
    TextView today_tasks_count, zak_tasks, vol_tasks, balance;
    ImageButton exit, to_my_profile;
    public static boolean isMapKitInitialized = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_global);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        exit = findViewById(R.id.exit);
        exit.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", -1);
            editor.apply();
            startActivity(new Intent(MainGlobal.this, Registration.class));
        });
        to_my_profile = findViewById(R.id.to_my_profile);
        to_my_profile.setOnClickListener(v -> {
            Intent intent= new Intent(MainGlobal.this, Profile.class);
            String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
            intent.putExtra("id", uid);
            startActivity(intent);
        });
        confirm_task = findViewById(R.id.confirm_task);
        confirm_task.setOnClickListener(v -> {
            startActivity(new Intent(MainGlobal.this, TasksToConfirmation.class));

        });
        volunteer_task = findViewById(R.id.volunteer_task);
        volunteer_task.setOnClickListener(v -> {
            startActivity(new Intent(MainGlobal.this, VolunteerTasks.class));
        });
        zak_task = findViewById(R.id.zak_task);
        zak_task.setOnClickListener(v -> {
            startActivity(new Intent(MainGlobal.this, ZakTasks.class));
        });
        balance = findViewById(R.id.balance);
        today_tasks_count = findViewById(R.id.today_task_count);
        new Thread(() -> {
            URL url = null;
            try {
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_count_of_today_tasks");
                Log.d("url", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject json = new JSONObject(response.toString());
                Log.d("Status request", json.getString("Status"));

                if (json.getString("Status").equals("ok")) {
                    runOnUiThread(() -> {
                        try {
                            today_tasks_count.setText(json.getString("count"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
        zak_tasks = findViewById(R.id.zak_tasks);
        vol_tasks = findViewById(R.id.vol_tasks);
        new Thread(() -> {
            String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
            URL url = null;
            try {
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_count_of_user_tasks_and_balance"
                        + "?uid=" + URLEncoder.encode(uid, "UTF-8")
                );
                Log.d("url", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject json = new JSONObject(response.toString());
                Log.d("Status request", json.getString("Status"));

                if (json.getString("Status").equals("ok")) {

                    runOnUiThread(() -> {
                        try {
                            vol_tasks.setText("Принятых заявок: " + json.getString("vol_count"));
                            zak_tasks.setText("Добавленных заявок: " + json.getString("zak_count"));
                            balance.setText(json.getString("balance"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

}