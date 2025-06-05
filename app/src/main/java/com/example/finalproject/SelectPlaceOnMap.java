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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("haveApiKey", true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AppData appData = AppData.getInstance();
        Boolean value = appData.getGlobalVariable();

        if (!value) {
            MapKitFactory.setApiKey("0125ed02-7d2f-4c15-b356-4165d801ff31");
            appData.setGlobalVariable(true);
        }
        MapKitFactory.initialize(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_place_on_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent1 = getIntent();
        String fromActivity = intent1.getStringExtra("activity_name");
        mapView = findViewById(R.id.mapview);
        Point centerPoint = new Point(55.748557, 37.613901);
        mapView.getMap().move(
                new CameraPosition(centerPoint, 14.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 5),
                null
        );
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        placemark = mapObjects.addPlacemark(centerPoint);
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
            }
        };

        mapView.getMap().addInputListener(inputListener);
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
                intent = new Intent(SelectPlaceOnMap.this, MainGlobal.class);
            } else if ("registration".equals(fromActivity)) {
                intent = new Intent(SelectPlaceOnMap.this, Registration.class);
                intent.putExtra("login", intent1.getStringExtra("login"));
                intent.putExtra("password", intent1.getStringExtra("password"));
            } else if ("add_task".equals(fromActivity)) {
                intent = new Intent(SelectPlaceOnMap.this, AddTask.class);
                intent.putExtra("description", intent1.getStringExtra("description"));
            } else {
                intent = new Intent(SelectPlaceOnMap.this, MainGlobal.class);
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
}