package com.example.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yandex.mapkit.MapKitFactory;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddTask extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    Button to_city, add, add_image;
    EditText description, cost;
    TextView info;
    ImageView imageView;

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            initializeComponents();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Изображение не выбрано", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeComponents();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeComponents() {
        AtomicReference<String> point_latitude = new AtomicReference<>("-0.0");
        AtomicReference<String> point_longitude = new AtomicReference<>("-0.0");
        AtomicReference<String> description_str = new AtomicReference<>("None");
        AtomicReference<String> cost_str = new AtomicReference<>("0");
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.template_image);
        cost = findViewById(R.id.cost);
        add_image = findViewById(R.id.add_image);
        add_image.setOnClickListener(v -> {
            openFileChooser();
        });
        to_city = findViewById(R.id.to_city);
        to_city.setOnClickListener(v -> {
            Intent intent = new Intent(AddTask.this, SelectPlaceOnMap.class);
            intent.putExtra("activity_name", "add_task");
            intent.putExtra("description", description.getText().toString());
            if (imageUri != null) {
                intent.putExtra("imageUri", imageUri.toString());
            } else {
                intent.putExtra("imageUri", "null");
            }
            intent.putExtra("cost", cost.getText().toString());
            startActivity(intent);
        });

        info = findViewById(R.id.info);
        add = findViewById(R.id.add);
        add.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", getFileName(imageUri),
                                    RequestBody.create(getContentResolver().openInputStream(imageUri).readAllBytes(),
                                            MediaType.parse(getContentResolver().getType(imageUri))))
                            .build();

                    String url = "http://" + BuildConfig.IP_PC + ":5050/api/add_task_from_mobile" +
                            "?coord=" + URLEncoder.encode(point_longitude.get(), "UTF-8") + ","
                            + URLEncoder.encode(point_latitude.get(), "UTF-8") +
                            "&uid=" + URLEncoder.encode(uid, "UTF-8") +
                            "&note=" + URLEncoder.encode(description_str.get(), "UTF-8") +
                            "&cost=" + URLEncoder.encode(cost.getText().toString(), "UTF-8")
                            ;
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody.toString());
                    Log.d("Status request", json.getString("Status"));

                    if (json.getString("Status").equals("ok")) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Задача добавлена", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddTask.this, ZakTasks.class));
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        new Thread(() -> {
            try {
                Intent intent1 = getIntent();
                point_latitude.set(intent1.getStringExtra("point_latitude"));
                point_longitude.set(intent1.getStringExtra("point_longitude"));
                description_str.set(intent1.getStringExtra("description"));
                if (intent1.getStringExtra("imageUri").equals("null")) {
                    imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.template_image);
                } else {
                    imageUri = Uri.parse(intent1.getStringExtra("imageUri"));
                }
                cost_str.set(intent1.getStringExtra("cost"));
                Log.d("sssss", point_latitude.get());
                runOnUiThread(() -> {
                    description.setText(description_str.get());
                    imageView.setImageURI(imageUri);
                    cost.setText(cost_str.get());
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
                runOnUiThread(() -> info.setText(finalCityString));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        if (point_latitude.get().equals("-0.0")) {
            info.setText("");
        }
        Log.d("start_add_task", point_latitude + ", " + point_longitude);
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}