package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class Profile extends AppCompatActivity {
    TextView count_placed, count_completed, balance,city;
    EditText name, contact_info;
    Button change_password, save_changes, to_zak_task, to_vol_task;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.login);
        count_placed = findViewById(R.id.added);
        count_completed = findViewById(R.id.completed);
        balance = findViewById(R.id.balance);
        city = findViewById(R.id.city);
        contact_info = findViewById(R.id.contact_info);
        to_vol_task = findViewById(R.id.to_vol_task);
        to_vol_task.setOnClickListener(v -> startActivity(new Intent(Profile.this, VolunteerTasks.class)));
        to_zak_task = findViewById(R.id.to_zak_task);
        to_zak_task.setOnClickListener(v -> startActivity(new Intent(Profile.this, ZakTasks.class)));
        save_changes = findViewById(R.id.save_changes);
        save_changes.setOnClickListener(v -> {
            new Thread(() -> {
                URL url = null;
                try {
                    url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/change_profile/" +  // обновление информации о пользователе
                            intent.getStringExtra("id" ) + "?name=" +
                            URLEncoder.encode(name.getText().toString(), "UTF-8") + "&contact_info=" +
                            URLEncoder.encode(contact_info.getText().toString(), "UTF-8"));
                    Log.d("url", url.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
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
                    if (json.getString("Status").equals("ok")){
                        runOnUiThread(() -> Toast.makeText(Profile.this, "Изменения сохранены", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(Profile.this, "Что-то пошло не так. Повторите позже", Toast.LENGTH_SHORT).show());
                    }
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });
        change_password = findViewById(R.id.change_password);
        change_password.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
            BottomChangePassword bottomSheet = BottomChangePassword.newInstance(uid);
            bottomSheet.show(Profile.this.getSupportFragmentManager(), bottomSheet.getTag());  // запуск фрагмента для смены пароля
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));

        if (!uid.equals(intent.getStringExtra("id"))) {
            name.setFocusable(false);
            contact_info.setFocusable(false);
            change_password.setVisibility(View.GONE);
        }
        new Thread(() -> {
            URL url = null;
            try {
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_profile/" + intent.getStringExtra("id"));  // получение информации о пользователе
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
                    JSONObject info = json.getJSONObject("info");
                    runOnUiThread(() -> {
                        try {
                            name.setText(info.getString("name"));  // выводим на фронтенд
                            count_completed.setText(info.getString("count_completed"));
                            count_placed.setText(info.getString("count_placed"));
                            balance.setText(info.getString("balance"));
                            city.setText(info.getString("city"));;
                            contact_info.setText(info.getString("contact_info"));
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