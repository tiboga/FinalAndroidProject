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


import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
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

public class Registration extends AppCompatActivity {
    Button to_city, to_login, submit;
    EditText login, password, contact_info;
    TextView city_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
        if (Integer.parseInt(uid) != -1){
            startActivity(new Intent(Registration.this, MainGlobal.class));
        }
        to_city = findViewById(R.id.to_city);
        to_login = findViewById(R.id.to_login);
        login = findViewById(R.id.text);
        password = findViewById(R.id.password);
        city_name = findViewById(R.id.text_obl);
        contact_info = findViewById(R.id.contact_info);
        submit = findViewById(R.id.submit);
        to_city.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, SelectPlaceOnMap.class);
            intent.putExtra("activity_name", "registration");
            intent.putExtra("login", login.getText().toString());
            intent.putExtra("password", password.getText().toString());
            intent.putExtra("contact_info", contact_info.getText().toString());
            startActivity(intent);
        });

        to_login.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });
        submit.setOnClickListener(v -> {
            if (password.getText().length() < 8) {
                Toast.makeText(this, "Длина пароля должна быть не меньше 8", Toast.LENGTH_SHORT).show();
            } else if (login.getText().toString().equals("")) {
                Toast.makeText(this, "Поле логина не должно быть пустым", Toast.LENGTH_SHORT).show();
            } else if (contact_info.getText().toString().equals("")){
                Toast.makeText(this, "Поле контактной информации не должно быть пустым", Toast.LENGTH_SHORT).show();
            } else if (city_name.getText().toString().equals("Здесь будет Ваш выбранный город")){
                Toast.makeText(this, "Вы не выбрали город", Toast.LENGTH_SHORT).show();

            }
            else {
                new Thread(() -> {
                    try {

                        URL url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/reg" +
                                "?username=" + URLEncoder.encode(login.getText().toString(), "UTF-8") +
                                "&password=" + URLEncoder.encode(password.getText().toString(), "UTF-8") +
                                "&city=" + URLEncoder.encode(city_name.getText().toString(), "UTF-8") +
                                "&contact_info=" + URLEncoder.encode(contact_info.getText().toString(), "UTF-8"));
                        Log.d("go to server" , url.toString());
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
                        Log.d("go to server" , "все ок");
                        JSONObject jsonObject = new JSONObject(response.toString());
                        if (jsonObject.getString("Status").equals("ok")) {
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String token = task.getResult();
                                            Log.d("FCM Token", token);

                                            new Thread(() -> {
                                                try {
                                                    URL url_for_add_fcm_token = new URL("http://" + BuildConfig.IP_PC + ":5050/api/add_fcm_token_to_user" +
                                                            "?token=" + URLEncoder.encode(token, "UTF-8") +
                                                            "&uid=" + URLEncoder.encode(String.valueOf(jsonObject.getInt("id")), "UTF-8")
                                                    );
                                                    Log.d("go to server", url_for_add_fcm_token.toString());
                                                    HttpURLConnection conn_for_add_fcm_token = (HttpURLConnection) url_for_add_fcm_token.openConnection();
                                                    conn_for_add_fcm_token.setRequestMethod("POST");
                                                    conn_for_add_fcm_token.setRequestProperty("Accept", "application/json");
                                                    conn_for_add_fcm_token.connect();

                                                    int responseCode = conn_for_add_fcm_token.getResponseCode();
                                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                                        BufferedReader reader_for_add_fcm_token = new BufferedReader(new InputStreamReader(conn_for_add_fcm_token.getInputStream()));
                                                        String line_for_add_fcm_token;
                                                        StringBuilder response_for_add_fcm_token = new StringBuilder();

                                                        while ((line_for_add_fcm_token = reader_for_add_fcm_token.readLine()) != null) {
                                                            response_for_add_fcm_token.append(line_for_add_fcm_token);
                                                        }
                                                        reader_for_add_fcm_token.close();

                                                        JSONObject jsonObject_for_add_fcm_token = new JSONObject(response_for_add_fcm_token.toString());
                                                        if (!jsonObject_for_add_fcm_token.getString("Status").equals("ok")) {
                                                            runOnUiThread(() -> Toast.makeText(Registration.this, "Не удалось сохранить токен уведомлений", Toast.LENGTH_SHORT).show());
                                                        }
                                                    } else {
                                                        runOnUiThread(() -> Toast.makeText(Registration.this, "Ошибка сервера при сохранении токена", Toast.LENGTH_SHORT).show());
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    runOnUiThread(() -> Toast.makeText(Registration.this, "Ошибка при отправке токена", Toast.LENGTH_SHORT).show());
                                                }
                                            }).start();
                                        } else {
                                            Log.e("FCM Token", "Ошибка получения токена", task.getException());
                                        }
                                    });

                            runOnUiThread(() -> {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                try {
                                    editor.putInt("user_id", jsonObject.getInt("id"));
                                    editor.apply();
                                    Toast.makeText(Registration.this, "Пользователь зарегистрирован!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Registration.this, MainGlobal.class);
                                    startActivity(intent);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else if (jsonObject.getString("Error").equals("username is exits")) {
                            runOnUiThread(() -> {

                                Toast.makeText(this, "Пользователь с таким логином уже есть", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("go to server", e.toString());
                    }

                }).start();
            }
        });
        new Thread(() -> {
            try {

                Intent intent1 = getIntent();

                String point_latitude = intent1.getStringExtra("point_latitude");
                String point_longitude = intent1.getStringExtra("point_longitude");
                String login_str = intent1.getStringExtra("login");
                String password1_str = intent1.getStringExtra("password");
                String contact_info_str = intent1.getStringExtra("contact_info");

                if (point_latitude == null || point_longitude == null) {
                    Log.d("Registration", "Координаты не переданы");
                    return;
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    login.setText(login_str);
                    password.setText(password1_str);
                    contact_info.setText(contact_info_str);
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
                JSONArray cityArr = city.getJSONObject("GeoObject")
                        .getJSONObject("metaDataProperty")
                        .getJSONObject("GeocoderMetaData")
                        .getJSONObject("Address")
                        .getJSONArray("Components");
                String cityString = null;
                for (int i = 0; i < cityArr.length(); i++) {
                    if (cityArr.getJSONObject(i).getString("kind").equals("locality")) {
                        cityString = cityArr.getJSONObject(i).getString("name");
                        break;
                    }
                }
                String finalCityString = cityString;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (finalCityString == null) {
                        city_name.setText("Выберите ближайший город");
                    } else {
                        city_name.setText(finalCityString);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}