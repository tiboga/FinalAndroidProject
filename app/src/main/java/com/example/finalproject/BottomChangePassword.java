package com.example.finalproject;

import static android.app.Activity.RESULT_OK;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BottomChangePassword extends BottomSheetDialogFragment {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int PICK_IMAGE_REQUEST = 1;
    EditText password_pred, passwort_current;
    Button confirm;
    public static BottomChangePassword newInstance(String id) {
        BottomChangePassword fragment = new BottomChangePassword();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_change_password, container, false);
        password_pred = view.findViewById(R.id.password_pred);
        passwort_current = view.findViewById(R.id.passwort_current);
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(v -> {
            Log.d("potrogalknopku", "potrogana");
            new Thread(() -> {
                try {
                    Bundle args = getArguments();
                    OkHttpClient client = new OkHttpClient();
                    String jsonInputString = String.format("{\"password_pred\": \"%s\", \"password_current\": \"%s\"}", password_pred.getText(), passwort_current.getText());
                    RequestBody requestBody = RequestBody.create(JSON, jsonInputString);
                    URL url = new URL("http://" + BuildConfig.IP_PC +
                            ":5050/api/change_password/" +
                            URLEncoder.encode(args.getString("id"), "UTF-8"));
                    Request request = new Request.Builder()
                            .url(url.toString())
                            .post(requestBody)
                            .build();
                    Log.d("go to server", url.toString());
                    try (Response response = client.newCall(request).execute()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getString("Status").equals("ok")){
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Пароль успешно сменен", Toast.LENGTH_SHORT).show());

                        } else {
                            if (jsonObject.getString("Error").equals("not valid password_pred")) {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Текущий пароль введен неверно", Toast.LENGTH_SHORT).show());
                            } else {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Что-то пошло не так. Повторите позже", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
}