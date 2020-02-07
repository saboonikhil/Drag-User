package com.drag.user.data;

import android.content.Context;

import com.drag.user.R;
import com.drag.user.model.Faq;

import java.util.ArrayList;
import java.util.List;

public class HelpFaqDBHelper {

    private static HelpFaqDBHelper fd;
    private static List<Faq> mFaqList = new ArrayList<>();

    private HelpFaqDBHelper(Context context) {
        mFaqList.add(new Faq(context.getString(R.string.help_faq_question_1), context.getString(R.string.help_faq_answer_1)));
        mFaqList.add(new Faq(context.getString(R.string.help_faq_question_2), context.getString(R.string.help_faq_answer_2)));
        mFaqList.add(new Faq(context.getString(R.string.help_faq_question_3), context.getString(R.string.help_faq_answer_3)));
        mFaqList.add(new Faq(context.getString(R.string.help_faq_question_4), context.getString(R.string.help_faq_answer_4)));
        mFaqList.add(new Faq(context.getString(R.string.help_faq_question_5), context.getString(R.string.help_faq_answer_5)));
    }

    public static List<Faq> getFaqDataList(Context context) {
        if (fd == null)
            fd = new HelpFaqDBHelper(context);

        return mFaqList;
    }
}