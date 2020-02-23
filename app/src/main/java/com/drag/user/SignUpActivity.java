package com.drag.user;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.drag.user.network.SignUpResponse;
import com.drag.user.network.SignUpUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private static String TAG = SignUpActivity.class.getSimpleName();
    private LinearLayout rootView;
    private ImageButton backView;
    private TextInputLayout passwordLayout, confirmPasswordLayout;
    private EditText nameView, emailView, contactView, passwordView, confirmPasswordView;
    private TextView termsOfUseView;
    private Button createAccountView;
    private InputMethodManager imm;
    private TextWatcher textWatcher;
    private String countryCode = "+91 ";
    private ProgressDialog pd;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();

        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contactView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                contactView.setFocusableInTouchMode(true);
                contactView.requestFocus();
                imm.showSoftInput(contactView, InputMethodManager.SHOW_IMPLICIT);
                return true;
            }
        });

        contactView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (contactView.getText().toString().length() == 0) {
                        contactView.setText(countryCode);
                        Selection.setSelection(contactView.getText(), contactView.getText().length());
                    }
                    imm.showSoftInput(contactView, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    if (contactView.getText().toString().equals(countryCode)) {
                        contactView.removeTextChangedListener(textWatcher);
                        contactView.getText().clear();
                        contactView.addTextChangedListener(textWatcher);
                    }
                }
            }
        });

        contactView.addTextChangedListener(textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith("+91 ")) {
                    contactView.setText(countryCode);
                    Selection.setSelection(contactView.getText(), contactView.getText().length());
                }
            }
        });

        passwordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        confirmPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        confirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    createUserAccount();
                    return true;
                }
                return false;
            }
        });

        String termsOfUsageText = "By creating an account, you agree to our Terms & Conditions and Privacy Policy.";
        SpannableString ss = new SpannableString(termsOfUsageText);
        ss.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        TermsOfUsageFragment termsOfUsage = new TermsOfUsageFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("display", "Terms & Conditions");
                        termsOfUsage.setArguments(bundle);
                        termsOfUsage.show(getSupportFragmentManager(), "Terms Of Usage");
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setColor(getResources().getColor(R.color.colorPrimary));
                    }
                }, termsOfUsageText.indexOf("Terms & Conditions"),
                termsOfUsageText.indexOf("Terms & Conditions") + "Terms & Conditions".length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        ss.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        TermsOfUsageFragment termsOfUsage = new TermsOfUsageFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("display", "Privacy Policy");
                        termsOfUsage.setArguments(bundle);
                        termsOfUsage.show(getSupportFragmentManager(), "Terms Of Usage");
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setColor(getResources().getColor(R.color.colorPrimary));
                    }
                }, termsOfUsageText.indexOf("Privacy Policy"),
                termsOfUsageText.indexOf("Privacy Policy") + "Privacy Policy".length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        termsOfUseView.setText(ss);
        termsOfUseView.setMovementMethod(LinkMovementMethod.getInstance());

        createAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUserAccount();
            }
        });
    }

    private void createUserAccount() {
        String name = nameView.getText().toString();
        String email = emailView.getText().toString();
        String contact = contactView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        passwordLayout.setErrorEnabled(false);
        confirmPasswordLayout.setErrorEnabled(false);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            focusView = nameView;
            cancel = true;
        } else if (!name.matches(".*[a-zA-Z]+.*")) {
            focusView = nameView;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            focusView = emailView;
            cancel = true;
        } else if (TextUtils.isEmpty(contact)) {
            focusView = contactView;
            cancel = true;
        } else if (contact.length() > 4 && contact.length() < 14) {
            focusView = contactView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            focusView = passwordView;
            cancel = true;
        } else if (password.length() > 0 && password.length() < 5) {
            focusView = passwordView;
            cancel = true;
        } else if (TextUtils.isEmpty(confirmPassword)) {
            focusView = confirmPasswordView;
            cancel = true;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Passwords do not match");
            focusView = confirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            focusView.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        } else {
            if (isConnectedToInternet()) {
                pd = ProgressDialog.show(this, "", "Creating account...", true, false);
                EndPointInterface service = APIUtils.getAPIService(SignUpActivity.this);
                service.createUser(name, email, contact, password).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        pd.dismiss();
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Log in to get started", Toast.LENGTH_SHORT).show();
                                }
                            }, 2000);
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            SignUpResponse error = SignUpUtils.parseError(response, SignUpActivity.this);
                            Toast.makeText(SignUpActivity.this, error.response(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        pd.dismiss();
                        Log.e(TAG + " On Failure", t.getMessage());
                        Snackbar.make(rootView, "Please check your internet connection or try again later.", Snackbar.LENGTH_LONG).show();
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
        rootView = findViewById(R.id.sign_up_activity_layout);
        backView = findViewById(R.id.sign_up_back);
        nameView = findViewById(R.id.sign_up_name);
        emailView = findViewById(R.id.sign_up_email);
        contactView = findViewById(R.id.sign_up_mobile_number);
        passwordLayout = findViewById(R.id.sign_up_password_layout);
        passwordView = findViewById(R.id.sign_up_password);
        confirmPasswordLayout = findViewById(R.id.sign_up_confirm_password_layout);
        confirmPasswordView = findViewById(R.id.sign_up_confirm_password);
        termsOfUseView = findViewById(R.id.sign_up_terms_of_use);
        createAccountView = findViewById(R.id.sign_up_button);
    }
}