package com.example.finalproject;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;


import java.util.concurrent.TimeUnit;

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

    /**
     *
     * @param id
     * @return
     */
    public static BottomConfirmation newInstance(Integer id) {
        BottomConfirmation fragment = new BottomConfirmation(); // добавление в аргументы данных, переданных после инициализации класса
        Bundle args = new Bundle();
        args.putString("id", id.toString());
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.bottom_confirmation, container, false);

        add_image = view.findViewById(R.id.add_photo);
        add_image.setOnClickListener(v -> openFileChooser());
        imageView = view.findViewById(R.id.imageView);
        add_to_confirmation = view.findViewById(R.id.confirm);
        Bundle args = getArguments();
        add_to_confirmation.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)  // устанавливаем большие лимиты по времени, чтобы можно было загружать фото с большим весом
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build(); //  создаем клиент
                    RequestBody requestBody = new MultipartBody.Builder()  // собираем тело
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
                            .build();  // запрос создаем
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string(); // читаем ответ сервера
                    JSONObject json = new JSONObject(responseBody.toString()); // читаем json из строки
                    Log.d("Status request", json.getString("Status"));
                    if (json.getString("Status").equals("ok")) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Задача отправлена на подтверждение", Toast.LENGTH_SHORT).show();  // уведомляем об успешном совершении действия
                            startActivity(new Intent(getActivity(), ZakTasks.class));
                        });
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Что-то пошло не так", Toast.LENGTH_SHORT).show());  // уведомляем о внутренней ошибке
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start(); // запускаем асинхронный поток
        });

        return view;
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
    }

    /**
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK  // получаем fileURI выбранного изображения
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(getActivity(), "Изображение не выбрано", Toast.LENGTH_SHORT).show();  // уведомляем о невыбранной  картинке
        }
    }

    /**
     *
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     *
     * @param uri
     * @return
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver().query(
                    uri, null, null, null, null)) {
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