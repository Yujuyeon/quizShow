package com.pedro.rtpstreamer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class correctOrNot extends AppCompatActivity
{
    TextView quizResultTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct_or_not);

        quizResultTV = findViewById(R.id.quizResult);

        Intent i = getIntent();
        String quizResult = i.getStringExtra("quizResult");

        if(quizResult.equals("x"))
        {
            quizResultTV.setText("틀렸습니다.");
        }
        else
        {
            quizResultTV.setText("정답입니니.");
        }
    }
}
