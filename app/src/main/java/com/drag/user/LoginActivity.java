package com.drag.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drag.user.model.Location;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.drag.user.util.ObjectSerializer;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    private SharedPreferences pref;
    private LinearLayout rootView;
    private EditText emailView, passwordView;
    private Button loginView;
    private LinearLayout signUpView;
    private InputMethodManager imm;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();

        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        loginView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                attemptLogin();
            }
        });

        signUpView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isTokenValid()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("view", "Book Ride");
            startActivity(intent);
            finish();
        } else {
            initLocations();
        }
    }

    private void initLocations() {
        EndPointInterface service = APIUtils.getAPIService(LoginActivity.this);
        service.initLocation().enqueue(new Callback<Location[]>() {
            @Override
            public void onResponse(@NonNull Call<Location[]> call, @NonNull Response<Location[]> response) {
                if (response.body() != null) {
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("locations", ObjectSerializer.serialize(response.body()));
                    edit.apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Location[]> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
            }
        });
    }

    private boolean isTokenValid() {
        String exp = pref.getString("expires", "");
        long time = System.currentTimeMillis();
        long expires;
        try {
            expires = Long.parseLong(exp);
        } catch (NumberFormatException nfe) {
            expires = 0;
        }
        return time < expires;
    }

    private void attemptLogin() {
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            focusView = emailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            focusView = passwordView;
            cancel = true;
        } else if (password.length() > 0 && password.length() < 5) {
            focusView = passwordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            focusView.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        } else {
            if (isConnectedToInternet()) {
                pd = ProgressDialog.show(this, "", "Dragging in...", true);
                EndPointInterface service = APIUtils.getAPIService(LoginActivity.this);
                service.authSignIn(email, password, "user").enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        pd.dismiss();
                        if (response.body() != null) {
                            if (response.body().res()) {
                                SharedPreferences.Editor edit = pref.edit();
                                edit.putString("token", response.body().token().token());
                                edit.putString("expires", response.body().token().expires());
                                edit.putString("dbObj", new Gson().toJson(response.body().token().user()));
                                edit.apply();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("view", "Book Ride");
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), response.body().response(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        pd.dismiss();
                        Log.e(TAG + " On Failure", t.getMessage());
                        Toast.makeText(getApplicationContext(), "Please check your internet connection or try again later.", Toast.LENGTH_LONG).show();
                    }
                });
            } else
                Snackbar.make(rootView, "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    private void initViews() {
        rootView = findViewById(R.id.login_activity_layout);
        emailView = findViewById(R.id.login_email);
        passwordView = findViewById(R.id.login_password);
        loginView = findViewById(R.id.login_button);
        signUpView = findViewById(R.id.login_sign_up);
    }
}