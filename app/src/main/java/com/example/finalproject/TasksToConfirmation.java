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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class TasksToConfirmation extends AppCompatActivity {
    Button onmain;
    RecyclerView list_of_task;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks_to_confirmation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        list_of_task = findViewById(R.id.list_of_tasks);
        list_of_task.setLayoutManager(new LinearLayoutManager(this));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", -1); // элемент загрузочный
            jsonObject.put("note", "Загружается");
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
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_tasks_to_confirmation" // получение добавленных на проверку заявок
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
                Log.d("Status request", json.getString("Status")); // парсинг ответа в json

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
            startActivity(new Intent(TasksToConfirmation.this, MainGlobal.class));
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button report, confirm, add_to_revision;
        JSONArray data;

        /**
         *
         * @param itemView
         * @param data
         */
        public MyViewHolder(View itemView, JSONArray data) {
            super(itemView);
            this.data = data;
            textView = itemView.findViewById(R.id.name);
            add_to_revision = itemView.findViewById(R.id.add_to_revision);
            add_to_revision.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject elem = data.getJSONObject(position);
                        Integer task_id = elem.getInt("id");
                        new Thread(() -> {
                            URL url = null;
                            try {
                                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/task_to_revision"  // отбраковка заявки
                                        + "?task_id=" + URLEncoder.encode(String.valueOf(task_id), "UTF-8")
                                );
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
                                Log.d("Status request", json.getString("Status"));
                                if (json.getString("Status").equals("ok")){
                                    runOnUiThread(() -> Toast.makeText(TasksToConfirmation.this, "Задача отправлена на доработку", Toast.LENGTH_SHORT).show());
                                } else {
                                    runOnUiThread(() -> Toast.makeText(TasksToConfirmation.this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show());
                                }
                            } catch (JSONException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            report = itemView.findViewById(R.id.report);
            report.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject elem = data.getJSONObject(position); // информация о заявке
                        Intent intent = new Intent(TasksToConfirmation.this, ImageViewer.class);
                        intent.putExtra("id", elem.getInt("id"));
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            confirm = itemView.findViewById(R.id.confirm);
            confirm.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject elem = data.getJSONObject(position);
                        Integer task_id = elem.getInt("id");
                        new Thread(() -> {
                            URL url = null;
                            try {
                                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/check_task" // принятие заявки
                                        + "?task_id=" + URLEncoder.encode(String.valueOf(task_id), "UTF-8")
                                );
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
                                JSONObject json = new JSONObject(response.toString()); // парсинг ответа в json
                                Log.d("Status request", json.getString("Status"));
                                if (json.getString("Status").equals("ok")){
                                    runOnUiThread(() -> Toast.makeText(TasksToConfirmation.this, "Задача одобрена", Toast.LENGTH_SHORT).show());
                                } else {
                                    runOnUiThread(() -> Toast.makeText(TasksToConfirmation.this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show());
                                }
                            } catch (JSONException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }}

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private JSONArray data;

        /**
         *
         * @param data
         */
        public MyAdapter(JSONArray data) {
            this.data = data;
        }

        /**
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         *               an adapter position.
         * @param viewType The view type of the new View.
         *
         * @return
         */
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_confirmation_tasks, parent, false);
            return new TasksToConfirmation.MyViewHolder(view, data);
        }

        /**
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the
         *        item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            try {
                JSONObject elem = data.getJSONObject(position);
                holder.textView.setText(elem.getString("note"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }

        /**
         *
         * @return
         */
        @Override
        public int getItemCount() {
            return data.length();
        }
    }
}