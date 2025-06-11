package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SearchTaskToVolunteer extends AppCompatActivity {
    Button onmain;
    RecyclerView list_of_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_task_to_volunteer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        list_of_task = findViewById(R.id.list_of_tasks);
        list_of_task.setLayoutManager(new LinearLayoutManager(this));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", -1);
            jsonObject.put("note", "Загружается");
            jsonObject.put("coord_1", 0.0);
            jsonObject.put("coord_2", 0.0);
            jsonObject.put("ended", true);
            jsonObject.put("created_on", "0000");
            jsonObject.put("username", "Не указано");
            jsonObject.put("formatted_address", "Нема");
            jsonObject.put("cost", 0);
            JSONArray data_init = new JSONArray();
            data_init.put(0, jsonObject);
            MyAdapter adapter_init = new MyAdapter(data_init);
            list_of_task.setAdapter(adapter_init);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
            URL url = null;
            try {
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/search_tasks_for_volunteer"
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
                    JSONArray data = json.getJSONArray("list");
                    MyAdapter adapter = new MyAdapter(data);
                    runOnUiThread(() -> {
                        list_of_task.setAdapter(adapter);
                    });
                } else {
                    Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
        onmain = findViewById(R.id.onmain);
        onmain.setOnClickListener(v -> {
            startActivity(new Intent(SearchTaskToVolunteer.this, MainGlobal.class));
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView, taskPlace, cost;
        Button button, add_task;
        JSONArray data;

        public MyViewHolder(@NonNull View itemView, JSONArray data) {
            super(itemView);
            this.data = data;
            textView = itemView.findViewById(R.id.textView);
            taskPlace = itemView.findViewById(R.id.tasksPlace);
            cost = itemView.findViewById(R.id.cost);
            add_task = itemView.findViewById(R.id.add);
            add_task.setOnClickListener(v -> {
                new Thread(() -> {
                    try {
                        int position = getAdapterPosition();
                        JSONObject elem = data.getJSONObject(position);
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
                        URL url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/add_task_to_user" +
                                "?item_id=" + URLEncoder.encode(String.valueOf(elem.getInt("id")), "UTF-8") +
                                "&user_id=" + URLEncoder.encode(uid, "UTF-8"));
                        Log.d("url connection", url.toString());
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
                        Log.d("Status request", json.getString("Status"));

                        if (json.getString("Status").equals("ok")) {
                            runOnUiThread(() -> {
                                Toast.makeText(SearchTaskToVolunteer.this, "Задача добавлена", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(SearchTaskToVolunteer.this, "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            });
            button = itemView.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject elem = data.getJSONObject(position);
                        BottomInfo bottomSheet = BottomInfo.newInstance(
                                elem.getInt("id"),
                                elem.getString("note"),
                                elem.getBoolean("ended"),
                                elem.getString("created_on"),
                                elem.getString("username"),
                                elem.getDouble("coord_1"),
                                elem.getDouble("coord_2"),
                                elem.getString("contact_info")
                        );
                        bottomSheet.show(((AppCompatActivity) itemView.getContext()).getSupportFragmentManager(), bottomSheet.getTag());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private JSONArray data;

        public MyAdapter(JSONArray data) {
            this.data = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_task_search_list, parent, false);
            return new MyViewHolder(view, data);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            try {
                JSONObject elem = data.getJSONObject(position);
                holder.textView.setText(elem.getString("note"));
                holder.taskPlace.setText(elem.getString("formatted_address"));
                holder.cost.setText(elem.getString("cost"));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public int getItemCount() {
            return data.length();
        }
    }
}