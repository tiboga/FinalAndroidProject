package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;

public class BottomInfo extends BottomSheetDialogFragment {
    private TextView note, ended, created_on, username_vol, contact_info;
    private Button get_images;
    private MapView mapView;
    private PlacemarkMapObject placemark;
    private Point centerPoint;

    /**
     *
     * @param id
     * @param note
     * @param ended
     * @param created_on
     * @param username
     * @param coord_1
     * @param coord_2
     * @param contact_info
     * @return
     */
    public static BottomInfo newInstance(Integer id, String note, Boolean ended, String created_on, String username, Double coord_1, Double coord_2, String contact_info) {
        BottomInfo fragment = new BottomInfo();  // сохраняем аргументы
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("note", note);
        args.putBoolean("ended", ended);
        args.putString("created_on", created_on);
        args.putString("username", username);
        args.putDouble("coord_1", coord_1);
        args.putDouble("coord_2", coord_2);
        args.putString("contact_info", contact_info);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppData appData = AppData.getInstance();
        Boolean value = appData.getGlobalVariable();
        if (!value) { // активация яндекс карт, если еще не инициализированы
            MapKitFactory.setApiKey("0125ed02-7d2f-4c15-b356-4165d801ff31");
            appData.setGlobalVariable(true);
            MapKitFactory.initialize(requireContext());

        }
    }

    /**
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_info, container, false);

        note = view.findViewById(R.id.note);
        ended = view.findViewById(R.id.ended);
        created_on = view.findViewById(R.id.created_on);
        username_vol = view.findViewById(R.id.username_vol);
        get_images = view.findViewById(R.id.get_photos);
        contact_info = view.findViewById(R.id.contacts);

        Bundle args = getArguments();  // получаем аргументы, переданные при запуске
        if (args != null) {
            note.setText(args.getString("note"));   // ставим значения-информацию об заявке
            boolean ended_val = args.getBoolean("ended");
            String ended_text = ended_val ? "✅ Выполнена" : "❌ Не выполнена";
            ended.setText(ended_text);
            created_on.setText(args.getString("created_on"));
            username_vol.setText(args.getString("username"));
            contact_info.setText(args.getString("contact_info"));
            mapView = view.findViewById(R.id.mapview);
            centerPoint = new Point(args.getDouble("coord_2"), args.getDouble("coord_1"));
            get_images.setOnClickListener(v -> {  // переход на просмотр фотографий
                Intent intent = new Intent(getActivity(), ImageViewer.class);
                intent.putExtra("id", args.getInt("id"));
                startActivity(intent);
            });
        }

        return view;
    }

    /**
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mapView != null && centerPoint != null) {
            mapView.getMap().move(  // анимация
                    new CameraPosition(centerPoint, 14.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 5),
                    null
            );
            mapView.getMap().setZoomGesturesEnabled(true);
            MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
            mapObjects.clear(); // очищаем старые объекты
            placemark = mapObjects.addPlacemark(centerPoint);  // добавляем точку заявки на карту
            placemark.setDraggable(false);  // запрещаем двигать метку
        }
    }

    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        MapKitFactory.getInstance().onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    /**
     *
     */
    @Override
    public void onStop() {
        if (mapView != null) {
            mapView.onStop();
        }
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}