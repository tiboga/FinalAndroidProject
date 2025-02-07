package com.example.finalproject;

import static com.example.finalproject.MainGlobal.isMapKitInitialized;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;

public class SelectPlaceOnMap extends AppCompatActivity {
    private MapView mapView;
    private PlacemarkMapObject placemark;
    private InputListener inputListener;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_place_on_map);
        if (!isMapKitInitialized) {
            MapKitFactory.setApiKey("0125ed02-7d2f-4c15-b356-4165d801ff31");
            MapKitFactory.initialize(this);
            isMapKitInitialized = true;
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Получаем данные из Intent
        Intent intent1 = getIntent();
        String fromActivity = intent1.getStringExtra("activity_name");

        // Инициализация MapView
        mapView = findViewById(R.id.mapview);

        // Устанавливаем начальную позицию камеры
        Point centerPoint = new Point(55.748557, 37.613901);
        mapView.getMap().move(
                new CameraPosition(centerPoint, 14.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 5),
                null
        );

        // Добавляем метку на карту
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        placemark = mapObjects.addPlacemark(centerPoint);

        // Обработка нажатия на карту
        inputListener = new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) {
                if (!isFinishing() && !isDestroyed() && placemark != null) {
                    placemark.setGeometry(point);
                    Log.d("MapTap", "Latitude: " + point.getLatitude() + ", Longitude: " + point.getLongitude());
                }
            }

            @Override
            public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
                // Обработка долгого нажатия (если нужно)
            }
        };

        mapView.getMap().addInputListener(inputListener);

        // Обработка нажатия на кнопку "Submit"
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(v -> {
            if (placemark == null || placemark.getGeometry() == null) {
                Log.e("SelectPlaceOnMap", "Метка не инициализирована или не имеет координат");
                return;
            }

            double latitude = placemark.getGeometry().getLatitude();
            double longitude = placemark.getGeometry().getLongitude();

            Intent intent;
            if ("main".equals(fromActivity)) {
                intent = new Intent(SelectPlaceOnMap.this, MainActivity.class);
            } else if ("registration".equals(fromActivity)) {
                intent = new Intent(SelectPlaceOnMap.this, Registration.class);
                intent.putExtra("login", intent1.getStringExtra("login"));
                intent.putExtra("password", intent1.getStringExtra("password"));
            } else if ("add_task".equals(fromActivity)){
                intent = new Intent(SelectPlaceOnMap.this, AddTask.class);
                intent.putExtra("description", intent1.getStringExtra("description"));
            } else {
                intent = new Intent(SelectPlaceOnMap.this, MainActivity.class);
            }

            intent.putExtra("point_latitude", String.valueOf(latitude));
            intent.putExtra("point_longitude", String.valueOf(longitude));


            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.getMap().removeInputListener(inputListener);
        }
        super.onDestroy();
    }
}