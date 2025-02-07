package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicReference;

public class AddTask extends AppCompatActivity {
    Button to_city, add;
    EditText description;
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AtomicReference<String> point_latitude = new AtomicReference<>("-0.0");
        AtomicReference<String> point_longitude = new AtomicReference<>("-0.0");
        AtomicReference<String> description_str = new AtomicReference<>("None");
        description = findViewById(R.id.description);
        to_city = findViewById(R.id.to_city);
        to_city.setOnClickListener(v -> {
            Intent intent = new Intent(AddTask.this, SelectPlaceOnMap.class);
            intent.putExtra("activity_name", "add_task");
            intent.putExtra("description", description.getText().toString());
            startActivity(intent);
        });
        info = findViewById(R.id.info);
        add = findViewById(R.id.add);
        add.setOnClickListener(v -> {
            new Thread(() -> {
                try {

                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    String uid = sharedPreferences.getString("user_id", "uid");
                    URL url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/add_task_from_mobile" +
                            "?coord=" + URLEncoder.encode(point_longitude.get(), "UTF-8") + ","
                            + URLEncoder.encode(point_longitude.get(), "UTF-8") +
                            "?uid=" + URLEncoder.encode(uid, "UTF-8") +
                            "?note=" + URLEncoder.encode(description_str.get(), "UTF-8")
                    );
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
                    if (json.getString("Status").equals("ok")) {
                        Toast.makeText(this, "Задача добавлена", Toast.LENGTH_SHORT);
                        startActivity(new Intent(AddTask.this, ZakTasks.class));
                    } else {
                        Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT);
                    }
                } catch (MalformedURLException | UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });
        new Thread(() -> {
            try {

                Intent intent1 = getIntent();

                point_latitude.set(intent1.getStringExtra("point_latitude"));
                point_longitude.set(intent1.getStringExtra("point_longitude"));
                description_str.set(intent1.getStringExtra("description"));
                Log.d("sssss", point_latitude.get());
                new Handler(Looper.getMainLooper()).post(() -> {
                    description.setText(description_str.get());
                });
                URL url = new URL("https://geocode-maps.yandex.ru/1.x/?apikey=216f2281-a0bc-4411-ac29-2723d14122fa&geocode=" + point_longitude + "," + point_latitude + "&format=json");
                Log.d("url for city name", "https://geocode-maps.yandex.ru/1.x/?apikey=216f2281-a0bc-4411-ac29-2723d14122fa&geocode=" + point_longitude + "," + point_latitude + "&format=json");
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
                JSONObject city = (JSONObject) json.getJSONObject("response")
                        .getJSONObject("GeoObjectCollection")
                        .getJSONArray("featureMember")
                        .get(0);
                String finalCityString = city.getJSONObject("GeoObject")
                        .getJSONObject("metaDataProperty")
                        .getJSONObject("GeocoderMetaData")
                        .getString("text");
                new Handler(Looper.getMainLooper()).post(() -> {

                    info.setText(finalCityString);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        if (point_latitude.get().equals("-0.0")){
            info.setText("");
        }
        Log.d("start_add_task", point_latitude + ", " + point_longitude);
    }
}