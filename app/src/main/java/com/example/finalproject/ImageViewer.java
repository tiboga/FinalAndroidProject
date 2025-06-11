package com.example.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageViewer extends AppCompatActivity {

    ImageView before_image, after_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        before_image = findViewById(R.id.before_image);
        after_image = findViewById(R.id.after_image);
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Intent intent = getIntent();
                Request request = new Request.Builder()
                        .url("http://" + BuildConfig.IP_PC + ":5050/api/get_image/before/" + intent.getIntExtra("id", -1))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        runOnUiThread(() -> {
                            before_image.setImageBitmap(bitmap);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ImageViewer.this,
                                "Ошибка загрузки: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ImageViewer.this,
                            "Ошибка: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Intent intent = getIntent();
                Request request = new Request.Builder()
                        .url("http://" + BuildConfig.IP_PC + ":5050/api/get_image/after/" + intent.getIntExtra("id", -1))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        runOnUiThread(() -> {
                            after_image.setImageBitmap(bitmap);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ImageViewer.this,
                                "Ошибка загрузки: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ImageViewer.this,
                            "Ошибка: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}