package com.drag.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TermsOfUsageFragment extends DialogFragment {

    private String display;
    private View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            display = getArguments().getString("display");
        }

        rootView = inflater.inflate(R.layout.fragment_terms_of_usage, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backView = rootView.findViewById(R.id.terms_of_usage_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TextView titleView = rootView.findViewById(R.id.terms_of_usage_title);
        titleView.setText(display);

        if (display.equals("Terms & Conditions")) {
            WebView contentView = rootView.findViewById(R.id.terms_of_usage_content);
            contentView.loadUrl("file:///android_asset/Terms & Conditions.html");
        } else if (display.equals("Privacy Policy")) {
            WebView contentView = rootView.findViewById(R.id.terms_of_usage_content);
            contentView.loadUrl("file:///android_asset/Privacy Policy.html");
        } else {
            WebView contentView = rootView.findViewById(R.id.terms_of_usage_content);
            contentView.loadUrl("file:///android_asset/Terms & Services.html");
        }
    }
}