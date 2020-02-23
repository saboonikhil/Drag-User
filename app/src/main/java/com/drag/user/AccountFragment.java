package com.drag.user;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.drag.user.model.User;
import com.drag.user.util.MaterialDialog;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private Activity parentActivity;
    private View rootView;
    private SharedPreferences pref;
    private LinearLayout profileView, paymentView;
    private TextView nameView, contactView, helpView, termsOfUsageView, logoutView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_account, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        pref = parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        String json = pref.getString("dbObj", "");
        User user = new Gson().fromJson(json, User.class);

        nameView.setText(user.getName());
        contactView.setText(user.getContact());

        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment profile = new ProfileFragment();
                profile.show(getChildFragmentManager(), "Profile");
            }
        });

        paymentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        helpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpFaqFragment helpFaq = new HelpFaqFragment();
                helpFaq.show(getChildFragmentManager(), "Help & FAQ");
            }
        });

        termsOfUsageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermsOfUsageFragment termsOfUsage = new TermsOfUsageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("display", "Terms & Services");
                termsOfUsage.setArguments(bundle);
                termsOfUsage.show(getChildFragmentManager(), "Terms Of Usage");
            }
        });

        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder().init(parentActivity)
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Logout",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pref.edit().remove("token").apply();
                                        pref.edit().remove("expires").apply();
                                        pref.edit().remove("dbObj").apply();
                                        Intent i = new Intent(parentActivity, LoginActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        parentActivity.finish();
                                    }
                                })
                        .setNegativeButton("Cancel")
                        .createMaterialDialog()
                        .show();
            }
        });
    }

    private void initViews() {
        nameView = rootView.findViewById(R.id.account_name);
        contactView = rootView.findViewById(R.id.account_mobile_number);
        profileView = rootView.findViewById(R.id.account_profile);
        paymentView = rootView.findViewById(R.id.account_payment);
        helpView = rootView.findViewById(R.id.account_help_faq);
        termsOfUsageView = rootView.findViewById(R.id.account_terms);
        logoutView = rootView.findViewById(R.id.account_logout);
    }
}