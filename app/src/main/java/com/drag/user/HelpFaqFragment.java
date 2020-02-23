package com.drag.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.drag.user.adapter.HelpFaqAdapter;
import com.drag.user.data.HelpFaqDBHelper;
import com.drag.user.model.Faq;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HelpFaqFragment extends DialogFragment {

    private static String TAG = HelpFaqFragment.class.getSimpleName();
    private View rootView;
    private Activity parentActivity;
    private ProgressDialog progressDialog;
    private ExpandableListView faqListView;
    private ImageButton backView;
    private FloatingActionButton feedbackView;
    private AlertDialog dialog;
    private ViewGroup container;
    private InputMethodManager imm;
    private User user;
    private String token;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_help_faq, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        container = (ViewGroup) rootView.getParent();
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

        setupFaqListViewData();

        feedbackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View customView = getLayoutInflater().inflate(R.layout.layout_feedback, container, false);
                TextView nameView = customView.findViewById(R.id.feedback_name);
                nameView.setText(user.getName());
                TextView contactView = customView.findViewById(R.id.feedback_contact);
                contactView.setText(user.getContact());
                final EditText messageView = customView.findViewById(R.id.feedback_message);
                Button cancelView = customView.findViewById(R.id.feedback_cancel);
                Button sendView = customView.findViewById(R.id.feedback_send);

                dialog = new AlertDialog.Builder(getContext()).setView(customView).create();
                dialog.show();

                cancelView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                sendView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendFeedback(messageView);
                    }
                });
            }
        });
    }

    private void setupFaqListViewData() {
        ArrayList<Map<String, String>> groupDataList = new ArrayList<>();
        List<Faq> faqList = HelpFaqDBHelper.getFaqDataList(getContext());

        Map<String, String> map;
        for (Faq question : faqList) {
            map = new HashMap<>();
            map.put("question", question.getQuestion());
            groupDataList.add(map);
        }

        String[] groupFrom = new String[]{"question"};
        int[] groupTo = new int[]{R.id.help_faq_question};
        ArrayList<ArrayList<Map<String, String>>> memberDataList = new ArrayList<>();

        for (int i = 0; i < faqList.size(); i++) {
            ArrayList<Map<String, String>> memberDataItemList = new ArrayList<>();
            map = new HashMap<>();
            map.put("answer", faqList.get(i).getAnswer());
            memberDataItemList.add(map);
            memberDataList.add(memberDataItemList);
        }

        String[] childFrom = new String[]{"answer"};
        int[] childTo = new int[]{R.id.help_faq_answer};

        HelpFaqAdapter adapter = new HelpFaqAdapter(getContext(),
                groupDataList, R.layout.layout_help_faq_question, groupFrom, groupTo,
                memberDataList, R.layout.layout_help_faq_answer, childFrom, childTo);

        faqListView.setAdapter(adapter);
    }

    private void sendFeedback(EditText messageView) {
        String feedbackText = messageView.getText().toString();
        if (feedbackText.length() > 40)
            pushFeedback(messageView);
        else
            Toast.makeText(getContext(), "Your message must be at least 40 characters long", Toast.LENGTH_LONG).show();
    }

    private void pushFeedback(final EditText messageView) {
        imm.hideSoftInputFromWindow(messageView.getWindowToken(), 0);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.sendFeedback(user.get_id(), user.getEmail(), token, messageView.getText().toString()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    progressDialog.cancel();
                    Toast.makeText(getContext(), "Message sent successfully", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e(TAG + " On Failure", t.getMessage());
                Toast.makeText(getContext(), "Please check your internet connection or try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        backView = rootView.findViewById(R.id.help_faq_back);
        faqListView = rootView.findViewById(R.id.help_faq_list);
        feedbackView = rootView.findViewById(R.id.help_faq_feedback);
    }
}