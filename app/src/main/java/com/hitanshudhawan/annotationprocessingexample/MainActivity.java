package com.hitanshudhawan.annotationprocessingexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hitanshudhawan.annotationprocessingexample.network.NetworkRequestBuilder;
import com.hitanshudhawan.annotationprocessingexample.network.NetworkResponse;
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

        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(
                new UserName("Hitanshu", "Dhawan"),
                "hitanshudhawan1996@gmail.com",
                29,
                true
        );
        NetworkResponse networkResponse = new NetworkRequestBuilder()
                .subUrl("http://example.com/api")
                .httpMethod("POST")
                .body(userRegistrationRequest)
                .build()
                .processSync();
        networkResponse.getSuccessResponse(UserRegistrationResponse.class);
    }
}
