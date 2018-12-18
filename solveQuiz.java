package com.pedro.rtpstreamer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class solveQuiz extends AppCompatActivity implements View.OnClickListener
{
    private String question, example1, example2, example3, quizNumber, userId;
    private String[] quizSet, exampleSet;
    private boolean isCheckedQuiz, isChecked1, isChecked2, isChecked3;
    private int userAnswer;

    //퀴즈 응답
    Handler handler;
    SocketChannel socketChannel;
    private static final String HOST = "192.168.1.7";
    private static final int QUIZ_PORT = 5002;

    TextView quizNumberTV, questionTV, example1TV, example2TV, example3TV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_quiz);

        questionTV = findViewById(R.id.question);
        quizNumberTV = findViewById(R.id.quizNumber);
        example1TV = findViewById(R.id.example1);
        example2TV = findViewById(R.id.example2);
        example3TV = findViewById(R.id.example3);

        example1TV.setOnClickListener(this);
        example2TV.setOnClickListener(this);
        example3TV.setOnClickListener(this);


        Intent i = getIntent();
        Log.d("chk", "받은 퀴즈:" + i.getStringExtra("quizSet"));

        quizSet = i.getStringExtra("quizSet").split("\\|");

        quizNumber = quizSet[0] + "번 문제";
        question = quizSet[1];
        exampleSet = quizSet[2].split("/");
        example1 = exampleSet[0];
        example2 = exampleSet[1];
        example3 = exampleSet[2];

        quizNumberTV.setText(quizNumber);
        questionTV.setText(question);
        example1TV.setText(example1);
        example2TV.setText(example2);
        example3TV.setText(example3);

        userId = "TEST";

        //5초 후 화면이 닫히는 핸들
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                handler = new Handler();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            socketChannel = SocketChannel.open();
                            socketChannel.configureBlocking(true);
                            socketChannel.connect(new InetSocketAddress(HOST, QUIZ_PORT));
                            new SendmsgTask().execute("userAnswer|"+quizNumber+"|"+userId+"|"+userAnswer);
                            Log.d("postQuiz", "5초 직");
                        }
                        catch (Exception ioe)
                        {
                            Log.d("asd", ioe.getMessage() + "a");
                            Log.d("postQuiz", "5초 직ss");
                            ioe.printStackTrace();

                        }
//                        checkUpdate.start();
                    }
                }).start();
                Intent i = new Intent(solveQuiz.this, watchingBroadcasting.class);
                i.putExtra("usrId", userId);
                startActivity(i);
                finish();
            }
        }, 5000);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
        {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed()
    {
        //안드로이드 백버튼 막기
        return;
    }

    @Override
    public void onClick(View v)
    {

        if(!isCheckedQuiz)
        {
            switch(v.getId())
            {
                case R.id.example1:
                        example1TV.setBackgroundColor(Color.rgb(255,0,0));
                        userAnswer = 1;
                        isCheckedQuiz = true;
                        break;

                case R.id.example2:
                        example2TV.setBackgroundColor(Color.rgb(0,255,0));
                        userAnswer = 2;
                        isCheckedQuiz = true;
                        break;

                case R.id.example3:
                        example3TV.setBackgroundColor(Color.rgb(0,0,255));
                        userAnswer = 3;
                        isCheckedQuiz = true;
                        break;
            }
        }
    }

private class SendmsgTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings)
        {
            try
            {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                }
            });
        }
    }}
