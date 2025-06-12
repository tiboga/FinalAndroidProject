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

public class EndedVolunteerTasks extends AppCompatActivity {
    Button onmain, add_new_task, not_ended_tasks;
    RecyclerView list_of_task;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ended_volunteer_tasks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        add_new_task = findViewById(R.id.add_new_task);
        add_new_task.setOnClickListener(v -> {
            startActivity(new Intent(EndedVolunteerTasks.this, SearchTaskToVolunteer.class));
        });
        not_ended_tasks = findViewById(R.id.not_ended_tasks);
        not_ended_tasks.setOnClickListener(v -> {
            startActivity(new Intent(EndedVolunteerTasks.this, VolunteerTasks.class));
        });
        list_of_task = findViewById(R.id.list_of_tasks);
        list_of_task.setLayoutManager(new LinearLayoutManager(this));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", -1);  // загрузочный элемент
            jsonObject.put("note", "Загружается");
            jsonObject.put("coord_1", 0.0);
            jsonObject.put("coord_2", 0.0);
            jsonObject.put("ended", true);
            jsonObject.put("created_on", "0000");
            jsonObject.put("username", "Не указано");
            jsonObject.put("cost", 0);
            JSONArray data_init = new JSONArray();
            data_init.put(0, jsonObject);
            MyAdapter adapter_init = new MyAdapter(data_init);
            list_of_task.setAdapter(adapter_init);  // инициализируем с одним загрузочным элементом
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
            URL url = null;
            try {  // получение списка законченных выполненных заявок
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_volunteer_task"
                        + "?uid=" + URLEncoder.encode(uid, "UTF-8")
                        + "&ended=" + URLEncoder.encode("true", "UTF-8")
                ); // собираем ссылку
                Log.d("url", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.connect(); // подключаемся к серверу
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())); // начинаем читать
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();  // заканчиваем читать ответ
                JSONObject json = new JSONObject(response.toString());
                Log.d("Status request", json.getString("Status"));

                if (json.getString("Status").equals("ok")) {
                    JSONArray data = json.getJSONArray("list"); // если все ок, то инициализируем адаптер с правильными данными
                    MyAdapter adapter = new MyAdapter(data);
                    runOnUiThread(() -> {
                        list_of_task.setAdapter(adapter);
                    });
                } else {
                    Toast.makeText(this,
                            "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                    // уведомляем об ошибке
                }
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
        onmain = findViewById(R.id.onmain);
        onmain.setOnClickListener(v -> {
            startActivity(new Intent(EndedVolunteerTasks.this, MainGlobal.class));
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView, cost;
        Button button, check_ended;
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
            cost = itemView.findViewById(R.id.balance);
            button = itemView.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                int position = getAdapterPosition(); // если нажимаем на кнопку о информации, то открываем диалог
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        JSONObject elem = data.getJSONObject(position);
                        BottomInfo bottomSheet = BottomInfo.newInstance(  // фрагмент с информацией о заявке
                                elem.getInt("id"),
                                elem.getString("note"),
                                elem.getBoolean("ended"),
                                elem.getString("created_on"),
                                elem.getString("username"),
                                elem.getDouble("coord_1"),
                                elem.getDouble("coord_2"),
                                elem.getString("contact_info")
                        );
                        bottomSheet.show(
                                ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager(),
                                bottomSheet.getTag()); // показ фрагмента
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_task_list, parent, false);
            return new EndedVolunteerTasks.MyViewHolder(view, data);
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
                holder.textView.setText(elem.getString("note"));  // добавление информации
                holder.cost.setText(elem.getString("cost"));

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