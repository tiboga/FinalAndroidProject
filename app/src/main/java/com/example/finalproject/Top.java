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

import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Top extends AppCompatActivity {
    Button onmain;
    RecyclerView list_of_task;
    TextView top_info;
    String object_ordering = "balance";
    MaterialButtonToggleGroup toggleGroup;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_top);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.balance) {
                        object_ordering = "balance";
                        top_info.setText("Топ по балансу"); // перезагрузка топа по балансу
                        refreshAdapter();
                    } else if (checkedId == R.id.count_placed) {
                        object_ordering = "count_placed";
                        top_info.setText("Топ по добавленным заявкам");
                        refreshAdapter(); // перезагрузка топа по добавленным заявкам
                    } else {
                        object_ordering = "count_completed";
                        top_info.setText("Топ по выполненным заявкам");
                        refreshAdapter(); // перезагрузка топа по выполненным заявкам
                    }
                }
            }
        });
        list_of_task = findViewById(R.id.list_of_tasks);
        list_of_task.setLayoutManager(new LinearLayoutManager(this));
        top_info = findViewById(R.id.top_info);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("index", "0");  // загрузочный элемент
            jsonObject.put("name", "Загружается");
            jsonObject.put("balance", "0");
            jsonObject.put("count_placed", "0");
            jsonObject.put("count_completed", "0");
            JSONArray data_init = new JSONArray();
            data_init.put(0, jsonObject);
            MyAdapter adapter_init = new MyAdapter(data_init);
            list_of_task.setAdapter(adapter_init);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        onmain = findViewById(R.id.onmain);
        onmain.setOnClickListener(v -> {
            startActivity(new Intent(Top.this, MainGlobal.class));
        });
        refreshAdapter();
    }

    /**
     *
     */
    public void refreshAdapter(){
        new Thread(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
            URL url = null;
            try {
                url = new URL("http://" + BuildConfig.IP_PC + ":5050/api/get_top_users/"  // ссылка для получения топа
                        + URLEncoder.encode(object_ordering, "UTF-8") + "/"  // по объекту object_ordering
                        + URLEncoder.encode(uid, "UTF-8")
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
                    JSONArray data = json.getJSONArray("users");
                    MyAdapter adapter = new MyAdapter(data);
                    runOnUiThread(() -> {
                        list_of_task.setAdapter(adapter); // ставим адаптер с нормальными данными
                    });
                } else {
                    Toast.makeText(this, "Что-то пошло не так. Попробуйте позже", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView index, name, balance, count_placed, count_completed;
        JSONArray data;

        /**
         *
         * @param itemView
         * @param data
         */
        public MyViewHolder(View itemView, JSONArray data) {
            super(itemView);
            this.data = data; // инициализация
            index = itemView.findViewById(R.id.index);
            name = itemView.findViewById(R.id.name);
            balance = itemView.findViewById(R.id.balance);
            count_placed = itemView.findViewById(R.id.count_placed);
            count_completed = itemView.findViewById(R.id.count_completed);

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_top, parent, false);
            return new Top.MyViewHolder(view, data);
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
                JSONObject elem = data.getJSONObject(position); // ставим информацию
                holder.index.setText(elem.getString("index"));
                holder.name.setText(elem.getString("name"));
                holder.balance.setText(elem.getString("balance"));
                holder.count_placed.setText(elem.getString("count_placed"));
                holder.count_completed.setText(elem.getString("count_completed"));

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