package com.example.finalproject;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONObject;

import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BottomConfirmation extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    Button add_image, add_to_confirmation;
    Uri imageUri;
    ImageView imageView;

    public static BottomConfirmation newInstance(Integer id) {
        BottomConfirmation fragment = new BottomConfirmation();
        Bundle args = new Bundle();
        args.putString("id", id.toString());
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_confirmation, container, false);

        add_image = view.findViewById(R.id.add_photo);
        add_image.setOnClickListener(v -> openFileChooser());
        imageView = view.findViewById(R.id.imageView);
        add_to_confirmation = view.findViewById(R.id.confirm);
        Bundle args = getArguments();
        add_to_confirmation.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", getFileName(imageUri),
                                    RequestBody.create(getActivity().getContentResolver().openInputStream(imageUri).readAllBytes(),
                                            MediaType.parse(getActivity().getContentResolver().getType(imageUri))))
                            .build();
                    String url = "http://" + BuildConfig.IP_PC + ":5050/api/add_task_to_confirmation/" + args.getString("id") + "/";
                    Log.d("go to server", url);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody.toString());
                    Log.d("Status request", json.getString("Status"));

                    if (json.getString("Status").equals("ok")) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Задача отправлена на подтверждение", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), ZakTasks.class));
                        });
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return view;
    }



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
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(getActivity(), "Изображение не выбрано", Toast.LENGTH_SHORT).show();
        }
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
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
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