package com.drag.user;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HelpFragment extends DialogFragment {

    private static String TAG = HelpFragment.class.getSimpleName();
    private View rootView;
    private Activity parentActivity;
    private InputMethodManager imm;
    private String token;
    private User user;
    private ImageButton backView;
    private Button callView, sendView;
    private EditText feedbackView;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_help, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        SharedPreferences pref = this.parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        String json = pref.getString("dbObj", "");
        token = pref.getString("token", "");
        user = new Gson().fromJson(json, User.class);

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        callView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAction();
            }
        });

        sendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
    }

    private void sendFeedback() {
        String feedbackText = feedbackView.getText().toString();
        if (feedbackText.length() > 40)
            pushFeedback();
        else
            Toast.makeText(getContext(), "Feedback must be at least 40 characters long", Toast.LENGTH_LONG).show();
    }

    private void pushFeedback() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Sending...");
        progressDialog.show();
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.sendFeedback(user.get_id(), user.getEmail(), token, feedbackView.getText().toString()).enqueue(new Callback<User>() {

            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    imm.hideSoftInputFromWindow(feedbackView.getWindowToken(), 0);
                    feedbackView.clearFocus();
                    feedbackView.getText().clear();
                    progressDialog.cancel();
                    Toast.makeText(getContext(), "Feedback sent successfully", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e(TAG + " On Failure", t.getMessage());
                Snackbar.make(rootView, "Please check your internet connection or try again later.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void callAction() {
        String helpDesk = "+91 7010823612";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + helpDesk));

        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Calling permission is revoked");
            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            Log.v("TAG", "Calling permission is granted");
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                callAction();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        backView = rootView.findViewById(R.id.help_back);
        callView = rootView.findViewById(R.id.help_call);
        feedbackView = rootView.findViewById(R.id.help_feedback);
        sendView = rootView.findViewById(R.id.help_send);
    }
}