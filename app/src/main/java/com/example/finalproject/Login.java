package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Login extends AppCompatActivity {
    Button to_registration, submit;
    EditText login, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        to_registration = findViewById(R.id.to_registration);
        to_registration.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Registration.class);
            startActivity(intent);
        });
        login = findViewById(R.id.text);
        password = findViewById(R.id.password);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    URL url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/login" +
                            "?username=" + URLEncoder.encode(login.getText().toString(), "UTF-8") +
                            "&password=" + URLEncoder.encode(password.getText().toString(), "UTF-8"));
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
                    JSONObject jsonObject = new JSONObject(response.toString());
                    if (jsonObject.getString("Status").equals("ok")) {
//                        FirebaseMessaging.getInstance().getToken()
//                                .addOnCompleteListener(task -> {
//                                    if (task.isSuccessful()) {
//                                        String token = task.getResult();
//                                        Log.d("FCM Token", token);
//                                        try {
//                                            URL url_for_add_fcm_token = new URL("http://" + BuildConfig.IP_PC + ":5050/api/add_fcm_token_to_user" +
//                                                    "?token=" + URLEncoder.encode(token, "UTF-8") +
//                                                    "&uid=" + URLEncoder.encode(String.valueOf(jsonObject.getInt("id")), "UTF-8")
//                                            );
//                                            Log.d("go to server" , url_for_add_fcm_token.toString());
//                                            HttpURLConnection conn_for_add_fcm_token = (HttpURLConnection) url_for_add_fcm_token.openConnection();
//                                            conn_for_add_fcm_token.setRequestMethod("POST");
//                                            conn_for_add_fcm_token.setRequestProperty("Accept", "application/json");
//                                            conn_for_add_fcm_token.connect();
//                                            BufferedReader reader_for_add_fcm_token = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                                            String line_for_add_fcm_token;
//                                            StringBuilder response_for_add_fcm_token = new StringBuilder();
//
//                                            while ((line_for_add_fcm_token = reader_for_add_fcm_token.readLine()) != null) {
//                                                response_for_add_fcm_token.append(line_for_add_fcm_token);
//                                            }
//                                            reader_for_add_fcm_token.close();
//                                            Log.d("go to server" , "все ок");
//                                            JSONObject jsonObject_for_add_fcm_token = new JSONObject(response_for_add_fcm_token.toString());
//                                            if (!jsonObject_for_add_fcm_token.getString("Status").equals("ok")) {
//                                                runOnUiThread(() -> {
//                                                    Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
//                                                });
//                                            }
//                                        } catch (JSONException | IOException e) {
//                                            throw new RuntimeException(e);
//                                        }
//                                    } else {
//                                        Log.e("FCM Token", "Ошибка получения токена", task.getException());
//
//                                    }
//                                });
                        runOnUiThread(() -> {
                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            try {
                                Log.d("idvzal", String.valueOf(jsonObject.getInt("id")));
                                editor.putInt("user_id", jsonObject.getInt("id"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            editor.apply();
                            Toast.makeText(this, "Вход успешен", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, MainGlobal.class);
                            startActivity(intent);
                        });
                    } else if (jsonObject.getString("Error").equals("username is not exists")) {
                        runOnUiThread(() -> {

                            Toast.makeText(this, "Нет пользователя с таким логином", Toast.LENGTH_SHORT).show();
                        });
                    } else if (jsonObject.getString("Error").equals("incorrect password")) {
                        runOnUiThread(() -> {

                            Toast.makeText(this, "Пароль не подходит", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        });
    }
}