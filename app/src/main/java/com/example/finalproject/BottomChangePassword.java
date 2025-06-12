package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BottomChangePassword extends BottomSheetDialogFragment {  // BottomSheetDialogFragment для измененмя пароля
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int PICK_IMAGE_REQUEST = 1;
    EditText password_pred, passwort_current;
    Button confirm;
    public static BottomChangePassword newInstance(String id) {
        BottomChangePassword fragment = new BottomChangePassword();  // добавление в аргументы данных, переданных после инициализации класса
        Bundle args = new Bundle();
        args.putString("id", id);
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
        View view = inflater.inflate(R.layout.bottom_change_password, container, false);
        password_pred = view.findViewById(R.id.password_pred);
        passwort_current = view.findViewById(R.id.passwort_current);
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Bundle args = getArguments();
                    OkHttpClient client = new OkHttpClient();
                    String jsonInputString = String.format("{\"password_pred\": \"%s\", \"password_current\": \"%s\"}", password_pred.getText(), passwort_current.getText());  // строка в виде json с предыдущим паролем и будущим паролем
                    RequestBody requestBody = RequestBody.create(JSON, jsonInputString);
                    URL url = new URL("http://" + BuildConfig.IP_PC +
                            ":5050/api/change_password/" +
                            URLEncoder.encode(args.getString("id"), "UTF-8"));
                    Request request = new Request.Builder() // собираем запрос
                            .url(url.toString())
                            .post(requestBody)
                            .build();
                    Log.d("go to server", url.toString());
                    try (Response response = client.newCall(request).execute()) {  // исполняем запрос
                        JSONObject jsonObject = new JSONObject(response.body().string());  // читаем json-ответ
                        if (jsonObject.getString("Status").equals("ok")){
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Пароль успешно сменен", Toast.LENGTH_SHORT).show());  // уведомляем об успешности действия
                        } else {
                            if (jsonObject.getString("Error").equals("not valid password_pred")) {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Текущий пароль введен неверно", Toast.LENGTH_SHORT).show());  // уведомляем об несоответствии пароля
                            } else {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Что-то пошло не так. Повторите позже", Toast.LENGTH_SHORT).show());  // уведомление о внутренней ошибке
                            }
                        }
                    }
                } catch (ProtocolException e) {  // просто catch ошибок
                    throw new RuntimeException(e);
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
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
}