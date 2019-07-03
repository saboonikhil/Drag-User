package com.drag.user;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ExpandableListView;

import com.drag.user.adapter.FaqsAdapter;
import com.drag.user.data.FaqDBHelper;
import com.drag.user.model.Faq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaqsFragment extends DialogFragment {

    private Activity parentActivity;
    private View rootView;
    private ExpandableListView exListView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_faqs, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity.setTitle("Frequently asked questions");
        setHasOptionsMenu(true);
        initViews();
        setupExListViewData();
    }

    private void setupExListViewData() {
        ArrayList<Map<String, String>> groupDataList = new ArrayList<>();
        List<Faq> faqList = FaqDBHelper.getFaqDataList(getContext());

        Map<String, String> map;
        for (Faq question : faqList) {
            map = new HashMap<>();
            map.put("question", question.getQuestion());
            groupDataList.add(map);
        }

        String[] groupFrom = new String[]{"question"};
        int[] groupTo = new int[]{R.id.faqs_question};
        ArrayList<ArrayList<Map<String, String>>> memberDataList = new ArrayList<>();

        for (int i = 0; i < faqList.size(); i++) {
            ArrayList<Map<String, String>> memberDataItemList = new ArrayList<>();
            map = new HashMap<>();
            map.put("answer", faqList.get(i).getAnswer());
            memberDataItemList.add(map);
            memberDataList.add(memberDataItemList);
        }

        String[] childFrom = new String[]{"answer"};
        int[] childTo = new int[]{R.id.faqs_answer};

        FaqsAdapter adapter = new FaqsAdapter(
                getContext(), groupDataList,
                R.layout.layout_faqs_question, groupFrom,
                groupTo, memberDataList, R.layout.layout_faqs_answer,
                childFrom, childTo);

        exListView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null)
            menu.removeItem(R.id.main_notifications);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window dialogWindow = getDialog().getWindow();
        if (dialogWindow != null) {
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }


    private void initViews() {
        exListView = rootView.findViewById(R.id.faqs_expandable_list);
    }
}