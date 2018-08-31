package com.hitanshudhawan.annotationprocessingexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hitanshudhawan.butterknife.ButterKnife;
import com.hitanshudhawan.butterknife_annotations.BindView;
import com.hitanshudhawan.butterknife_annotations.OnClick;

public class MainActivity extends AppCompatActivity {

    private int numberOfTimesTextViewClicked = 0;

    @BindView(R.id.text_view)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.text_view)
    void onTextViewClicked(View view) {
        textView.setText(String.valueOf(++numberOfTimesTextViewClicked));
    }
}
