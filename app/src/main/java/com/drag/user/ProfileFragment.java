package com.drag.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends DialogFragment {

    private static String TAG = ProfileFragment.class.getSimpleName();
    private Fragment parentFragment;
    private Activity parentActivity;
    private View rootView;
    private ViewGroup container;
    private ImageButton backView, editView, saveView;
    private SharedPreferences pref;
    private User user;
    private TextView nameView, emailView, contactView, alternateContactView, passwordView;
    private TextView profileEmailView, profileContactView, profileAlternateContactView, profilePasswordView;
    private ConnectivityManager connMgr;
    private AlertDialog dialog;
    private EditText editNameView, editEmailView, editContactView, editAlternateContactView,
            newPasswordView, confirmNewPasswordView;
    private String countryCode = "+91 ", token;
    private TextInputLayout confirmNewPasswordLayout;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentFragment = getParentFragment();
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        container = (ViewGroup) rootView.getParent();
        connMgr = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        pref = this.parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        String json = pref.getString("dbObj", "");
        token = pref.getString("token", "");
        user = new Gson().fromJson(json, User.class);

        nameView.setText(user.getName());
        emailView.setText(user.getEmail());
        contactView.setText(user.getContact());
        alternateContactView.setText(user.getAlternateContact());

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateEditProfile();
            }
        });

        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedProfile();
            }
        });
    }

    private void activateEditProfile() {
        if (isConnectedToInternet()) {
            nameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_blue, 0);
            emailView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_blue, 0);
            contactView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_blue, 0);
            alternateContactView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_blue, 0);
            passwordView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_blue, 0);
            profileEmailView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            profileContactView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            profileAlternateContactView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            profilePasswordView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            editView.setVisibility(View.GONE);
            saveView.setVisibility(View.VISIBLE);
            nameView.setEnabled(true);
            emailView.setEnabled(true);
            contactView.setEnabled(true);
            alternateContactView.setEnabled(true);
            passwordView.setEnabled(true);

            nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View customView = getLayoutInflater().inflate(R.layout.layout_edit_profile_name, container, false);
                    editNameView = customView.findViewById(R.id.edit_profile_name);
                    editNameView.setText(nameView.getText());

                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Edit Name")
                            .setView(customView)
                            .setPositiveButton("Submit", null)
                            .setNegativeButton("Cancel", null)
                            .create();

                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface di) {
                            dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String newName = editNameView.getText().toString();
                                    nameView.setText(newName);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog.show();
                    togglePositiveButton(false);

                    editNameView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (TextUtils.isEmpty(s.toString()) || s.toString().equals(user.getName())
                                    || !s.toString().matches(".*[a-zA-Z]+.*")) {
                                togglePositiveButton(false);
                            } else {
                                togglePositiveButton(true);
                            }
                        }
                    });
                    setKeyboardFocus(editNameView);
                }
            });

            emailView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View customView = getLayoutInflater().inflate(R.layout.layout_edit_profile_email, container, false);
                    editEmailView = customView.findViewById(R.id.edit_profile_email);
                    editEmailView.setText(emailView.getText());

                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Edit Email")
                            .setView(customView)
                            .setPositiveButton("Submit", null)
                            .setNegativeButton("Cancel", null)
                            .create();

                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface di) {
                            dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String newEmail = editEmailView.getText().toString();
                                    emailView.setText(newEmail);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog.show();
                    togglePositiveButton(false);

                    editEmailView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (TextUtils.isEmpty(s.toString()) || s.toString().equals(user.getEmail())
                                    || !isEmailValid(s.toString()))
                                togglePositiveButton(false);
                            else
                                togglePositiveButton(true);

                        }
                    });
                    setKeyboardFocus(editEmailView);
                }
            });

            contactView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View customView = getLayoutInflater().inflate(R.layout.layout_edit_profile_contact, container, false);
                    editContactView = customView.findViewById(R.id.edit_profile_mobile_number);
                    editContactView.setText(contactView.getText());

                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Edit Mobile Number")
                            .setView(customView)
                            .setPositiveButton("Submit", null)
                            .setNegativeButton("Cancel", null)
                            .create();

                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface di) {
                            dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String newContact = editContactView.getText().toString();
                                    contactView.setText(newContact);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog.show();
                    togglePositiveButton(false);

                    editContactView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (!s.toString().startsWith("+91 ")) {
                                editContactView.setText(countryCode);
                                Selection.setSelection(editContactView.getText(), editContactView.getText().length());
                            } else if (s.toString().equals(user.getContact())
                                    || s.toString().equals(alternateContactView.getText().toString())
                                    || s.toString().length() > 4 && s.toString().length() < 14) {
                                togglePositiveButton(false);
                            } else {
                                togglePositiveButton(true);
                            }
                        }
                    });
                    setKeyboardFocus(editContactView);
                }
            });

            alternateContactView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View customView = getLayoutInflater().inflate(R.layout.layout_edit_profile_alternate_contact, container, false);
                    editAlternateContactView = customView.findViewById(R.id.edit_profile_alternate_number);

                    if (alternateContactView.getText().length() == 0)
                        editAlternateContactView.setText(countryCode);
                    else editAlternateContactView.setText(alternateContactView.getText());

                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Edit Alternate Number")
                            .setView(customView)
                            .setPositiveButton("Submit", null)
                            .setNegativeButton("Cancel", null)
                            .create();

                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface di) {
                            dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String newAlternateContact = editAlternateContactView.getText().toString();
                                    alternateContactView.setText(newAlternateContact);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog.show();
                    togglePositiveButton(false);

                    editAlternateContactView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (!s.toString().startsWith("+91 ")) {
                                editAlternateContactView.setText(countryCode);
                                Selection.setSelection(editAlternateContactView.getText(), editAlternateContactView.getText().length());
                            } else if (s.toString().equals(user.getAlternateContact())
                                    || s.toString().equals(contactView.getText().toString())
                                    || s.toString().length() < 14) {
                                togglePositiveButton(false);
                            } else {
                                togglePositiveButton(true);
                            }
                        }
                    });
                    setKeyboardFocus(editAlternateContactView);
                }
            });

            passwordView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View customView = getLayoutInflater().inflate(R.layout.layout_edit_profile_password, container, false);
                    newPasswordView = customView.findViewById(R.id.edit_profile_new_password);
                    confirmNewPasswordView = customView.findViewById(R.id.edit_profile_confirm_new_password);
                    confirmNewPasswordLayout = customView.findViewById(R.id.edit_profile_confirm_new_password_layout);

                    dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Change Password")
                            .setView(customView)
                            .setPositiveButton("Submit", null)
                            .setNegativeButton("Cancel", null)
                            .create();

                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface di) {
                            dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String newPassword = newPasswordView.getText().toString();
                                    String confirmNewPassword = confirmNewPasswordView.getText().toString();
                                    if (!confirmNewPassword.equals(newPassword)) {
                                        confirmNewPasswordLayout.setError("Passwords do not match");
                                        confirmNewPasswordView.requestFocus();
                                    } else {
                                        dialog.dismiss();
                                        progressDialog = new ProgressDialog(getContext());
                                        progressDialog.setMessage("Updating...");
                                        progressDialog.show();
                                        savePassword(newPassword);
                                        deactivateEditProfile();
                                    }
                                }
                            });
                        }
                    });
                    dialog.show();
                    togglePositiveButton(false);

                    newPasswordView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            confirmNewPasswordLayout.setErrorEnabled(false);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.toString().length() >= 5 && confirmNewPasswordView.getText().toString().length() >= 5) {
                                togglePositiveButton(true);
                            } else {
                                togglePositiveButton(false);
                            }
                        }
                    });

                    confirmNewPasswordView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            confirmNewPasswordLayout.setErrorEnabled(false);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.toString().length() >= 5 && newPasswordView.getText().toString().length() >= 5) {
                                togglePositiveButton(true);
                            } else {
                                togglePositiveButton(false);
                            }
                        }
                    });
                    setKeyboardFocus(newPasswordView);
                }
            });
        } else {
            Snackbar.make(rootView, "No Internet Connection", Snackbar.LENGTH_LONG).show();
        }
    }

    private void saveEditedProfile() {
        if ((user.getName() != nameView.getText()) || (user.getEmail() != emailView.getText()) ||
                (user.getContact() != contactView.getText()) || ((alternateContactView.getText().toString().length() > 0)
                && user.getAlternateContact() == null) || (user.getAlternateContact() != null &&
                user.getAlternateContact() != alternateContactView.getText()))
            promptPassword();

        deactivateEditProfile();
    }

    private void promptPassword() {
        View customView = getLayoutInflater().inflate(R.layout.layout_edit_profile_prompt_password, container, false);
        EditText promptPasswordView = customView.findViewById(R.id.edit_profile_prompt_password);

        dialog = new AlertDialog.Builder(getContext())
                .setTitle("Enter Password")
                .setView(customView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Saving...");
                        progressDialog.show();
                        saveProfile();
                    }
                });
                dialog.getButton(di.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfileFragment profile = new ProfileFragment();
                        profile.show(parentFragment.getChildFragmentManager(), "Profile");
                        dismiss();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
        togglePositiveButton(false);

        promptPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 6)
                    togglePositiveButton(false);
                else
                    togglePositiveButton(true);
            }
        });
        setKeyboardFocus(promptPasswordView);
    }

    private void saveProfile() {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.userUpdate(user.get_id(), user.getEmail(), token, nameView.getText().toString(), emailView.getText().toString(),
                contactView.getText().toString(), alternateContactView.getText().toString()).enqueue(new Callback<User>() {

            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.body() != null) {
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("dbObj", new Gson().toJson(response.body()));
                    edit.apply();
                    user.setEmail(response.body().getEmail());
                    progressDialog.cancel();
                    Toast.makeText(getContext(), "Profile saved successfully", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                ProfileFragment profile = new ProfileFragment();
                profile.show(parentFragment.getChildFragmentManager(), "Profile");
                dismiss();
                progressDialog.cancel();
                Log.e(TAG + " On Failure", t.getMessage());
                Snackbar.make(rootView, "Please check your internet connection or try again later.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void savePassword(String password) {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.updatePassword(user.get_id(), user.getEmail(), token, password).enqueue(new Callback<User>() {

            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    progressDialog.cancel();
                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_LONG).show();
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

    private void deactivateEditProfile() {
        nameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        emailView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        contactView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        alternateContactView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        passwordView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        profileEmailView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, 0, 0);
        profileContactView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mobile_number, 0, 0, 0);
        profileAlternateContactView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alternate_number, 0, 0, 0);
        profilePasswordView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, 0, 0);
        saveView.setVisibility(View.GONE);
        editView.setVisibility(View.VISIBLE);
        nameView.setEnabled(false);
        emailView.setEnabled(false);
        contactView.setEnabled(false);
        alternateContactView.setEnabled(false);
        passwordView.setEnabled(false);
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void togglePositiveButton(boolean enable) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enable);
    }

    private void setKeyboardFocus(final EditText et) {
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                et.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                et.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                et.setSelection(et.getText().length());
            }
        }, 100);
    }

    private boolean isConnectedToInternet() {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void initViews() {
        backView = rootView.findViewById(R.id.profile_back);
        editView = rootView.findViewById(R.id.profile_edit);
        saveView = rootView.findViewById(R.id.profile_save);
        nameView = rootView.findViewById(R.id.profile_name);
        profileEmailView = rootView.findViewById(R.id.profile_email_tv);
        emailView = rootView.findViewById(R.id.profile_email);
        profileContactView = rootView.findViewById(R.id.profile_mobile_number_tv);
        contactView = rootView.findViewById(R.id.profile_mobile_number);
        profileAlternateContactView = rootView.findViewById(R.id.profile_alternate_number_tv);
        alternateContactView = rootView.findViewById(R.id.profile_alternate_number);
        profilePasswordView = rootView.findViewById(R.id.profile_password_tv);
        passwordView = rootView.findViewById(R.id.profile_password);
    }
}