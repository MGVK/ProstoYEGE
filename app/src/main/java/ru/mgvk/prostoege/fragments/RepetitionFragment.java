package ru.mgvk.prostoege.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.exercises.AnswerLayout;
import ru.mgvk.prostoege.ui.exercises.DescriptionWebView;
import ru.mgvk.prostoege.ui.exercises.NumPad;

public class RepetitionFragment extends Fragment
        implements View.OnClickListener, TextWatcher, NumPad.OnKeyClicked {

    private String             answer     = "";
    private String             htmlFile   = "";
    private DescriptionWebView descriptionWebView;
    private int                taskNumber = 0;
    private AnswerLayout       answerLayout;
    private NumPad             numpad;

    public static RepetitionFragment newInstance(int taskNumber, String htmlFilePath) {
        RepetitionFragment fragment = new RepetitionFragment();
        fragment.setTaskNumber(taskNumber);
        fragment.setHtmlFile(htmlFilePath);
        return fragment;
    }

    public void setHtmlFile(String htmlFile) {
        this.htmlFile = htmlFile;
    }

    public void setTaskNumber(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_repetition, container, false);
        descriptionWebView = (DescriptionWebView) rootView.findViewById(R.id.webview);
        descriptionWebView.init();
        descriptionWebView.loadHTMLFile(htmlFile);
        answerLayout = (AnswerLayout) rootView.findViewById(R.id.answerlayout);
        answerLayout.getAnswerTextView().addTextChangedListener(this);
        numpad = (NumPad) rootView.findViewById(R.id.numpad);
        numpad.init(this);
        numpad.setAnswerLayout(answerLayout);
        ((TextView) rootView.findViewById(R.id.task_number)).setText("Задание №" + taskNumber);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (answerLayout != null) {
            answerLayout.getAnswerTextView().setText(answer);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            answer = savedInstanceState.getString("answer");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("answer", answer);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        answer = editable.toString();
    }

    public void reload() {
//        descriptionWebView.reload();
    }

    @Override
    public void onKeyClicked(Button b, String s) {
        answer = answerLayout.getAnswer();
    }
}
