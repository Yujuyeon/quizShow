package com.pedro.rtpstreamer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class correctOrNot extends AppCompatActivity
{
    TextView quizResultTV;
    ImageView quizResultIMG;
    boolean quizResult, isWinner;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct_or_not);

        quizResultTV = findViewById(R.id.quizResult);
        quizResultIMG = findViewById(R.id.quizResultImg);

        Intent i = getIntent();
//        String quizResult = i.getStringExtra("quizResult");
        quizResult = i.getBooleanExtra("quizResult", false);
        isWinner = i.getBooleanExtra("isWinner", false);

        if(quizResult)
        {
            if(isWinner)
            {
                quizResultTV.setText("우승");
                quizResultIMG.setImageResource(R.drawable.win);
            }
            else
            {
                quizResultTV.setText("정답");
                quizResultIMG.setImageResource(R.drawable.redo);

            }
        }
        else
        {
            quizResultTV.setText("오답");
            quizResultIMG.setImageResource(R.drawable.redx);
//            quizResultIMG.setImageResource(R.drawable.redx);
        }
    }
}
