package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

public class ZakTasks extends AppCompatActivity {
    Button onmain, add_new_task, endedtasks;
    RecyclerView list_of_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_zak_tasks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        add_new_task = findViewById(R.id.add_new_task);
        add_new_task.setOnClickListener(v -> {
            startActivity(new Intent(ZakTasks.this, AddTask.class));
        });
        list_of_task = findViewById(R.id.list_of_tasks);
        list_of_task.setLayoutManager(new LinearLayoutManager(this));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("note", "Загружается");
            jsonObject.put("coord_1", 0.0);
            jsonObject.put("coord_2", 0.0);
            jsonObject.put("ended", true);
            jsonObject.put("created_on", "0000");
            jsonObject.put("username", "Не указано");
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
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_user_task"
                        + "?uid=" + URLEncoder.encode(uid, "UTF-8")
                        + "&ended=" + URLEncoder.encode("false", "UTF-8")
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
            startActivity(new Intent(ZakTasks.this, MainGlobal.class));
        });
        endedtasks = findViewById(R.id.ended_tasks);
        endedtasks.setOnClickListener(v -> {
            startActivity(new Intent(ZakTasks.this, EndedZakTasks.class));
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView image;
        Button button;
        JSONArray data;

        public MyViewHolder(@NonNull View itemView, JSONArray data) {
            super(itemView);
            this.data = data;
            textView = itemView.findViewById(R.id.textView);
            image = itemView.findViewById(R.id.imageView);
            button = itemView.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject elem = data.getJSONObject(position);
                        BottomInfo bottomSheet = BottomInfo.newInstance(
                                elem.getString("note"),
                                elem.getBoolean("ended"),
                                elem.getString("created_on"),
                                elem.getString("username"),
                                elem.getDouble("coord_1"),
                                elem.getDouble("coord_2")
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_task_list, parent, false);
            return new MyViewHolder(view, data);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            try {
                JSONObject elem = data.getJSONObject(position);
                holder.textView.setText(elem.getString("note"));
                if (elem.getBoolean("ended")) {
                    holder.image.setImageResource(R.drawable.ok);
                }
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