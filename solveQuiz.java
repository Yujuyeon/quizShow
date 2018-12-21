package com.pedro.rtpstreamer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

import static java.lang.Thread.sleep;

public class solveQuiz extends AppCompatActivity implements View.OnClickListener
{
    private String question, example1, example2, example3, userId, data;
    private String[] quizSet, exampleSet;
    private boolean isCheckedQuiz, isChecked1, isChecked2, isChecked3;
    private int quizNumber, userAnswer;
    //퀴즈 서비스 변수
    quizService quizService;
    boolean isService;
    ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            quizService.MyBinder myBinder = (quizService.MyBinder) service;
            quizService = myBinder.getService();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            isService = false;
        }
    };

    //퀴즈 응답
    Handler handler;
    SocketChannel socketChannel;
    private static final String HOST = "192.168.1.7";
    private static final int QUIZ_PORT = 5002;

    Handler countDownHandler;
    int countDownNumber;

    TextView quizNumberTV, questionTV, example1TV, example2TV, example3TV, countDownTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_quiz);

        Intent intent = new Intent(this, quizService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        questionTV = findViewById(R.id.question);
        quizNumberTV = findViewById(R.id.quizNumber);
        example1TV = findViewById(R.id.example1);
        example2TV = findViewById(R.id.example2);
        example3TV = findViewById(R.id.example3);

        example1TV.setOnClickListener(this);
        example2TV.setOnClickListener(this);
        example3TV.setOnClickListener(this);

        countDownTV = findViewById(R.id.countDown);
        countDownHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                countDownTV.setText(msg.arg1+"");
                countDownNumber--;
            }
        };

        Thread countDownThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                countDownNumber = 5;
                while(countDownNumber > 0)
                {
                    Message msg = countDownHandler.obtainMessage();
                    msg.arg1 = countDownNumber;
                    countDownHandler.sendMessage(msg);
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        countDownThread.start();


        Intent i = getIntent();
        Log.d("chk", "받은 퀴즈:" + i.getStringExtra("quizSet"));

        quizSet = i.getStringExtra("quizSet").split("\\|");

        quizNumber = Integer.parseInt(quizSet[0]);
        question = quizSet[1];
        exampleSet = quizSet[2].split("/");
        example1 = exampleSet[0];
        example2 = exampleSet[1];
        example3 = exampleSet[2];

        quizNumberTV.setText(quizNumber + "번 문제");
        questionTV.setText(question);
        example1TV.setText(example1);
        example2TV.setText(example2);
        example3TV.setText(example3);

        userAnswer = 0;

//        final Intent intent = getIntent();
//        userId = intent.getStringExtra("userId");

        Log.d("quizServer", "0");


        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                quizService.getData(com.pedro.rtpstreamer.quizService.getDataPurpose.ANSWER, quizNumber, userAnswer);
                finish();
            }
        }, 5000);




        //5초 후 화면이 닫히는 핸들
//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                handler = new Handler();
//                new Thread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        try
//                        {
//                            Log.d("quizServer", "1");
//                            socketChannel = SocketChannel.open();
//                            socketChannel.configureBlocking(true);
//                            socketChannel.connect(new InetSocketAddress(HOST, QUIZ_PORT));
////                            new SendmsgTask().execute("userAnswer|"+quizNumber+"|"+userId+"|"+userAnswer);
//                            new SendmsgTask().execute("userAnswer/"+quizNumber+"/"+userAnswer);
//                        }
//                        catch (Exception ioe)
//                        {
//                            ioe.printStackTrace();
//                        }
//                        checkUpdate.start();
//
//                    }
//                }).start();
//                Log.d("quizServer", "4");
//                Intent i = new Intent(solveQuiz.this, watchingBroadcasting.class);
//                i.putExtra("userId", userId);
//                i.putExtra("correctOrNot", data);
//                Log.d("quizServer", "solveShow: " + data);
//
//                startActivity(i);
//
//                finish();
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

        if (!isCheckedQuiz)
        {
            switch (v.getId())
            {
                case R.id.example1:
                    example1TV.setBackgroundColor(Color.rgb(0, 255, 0));
                    userAnswer = 1;
                    isCheckedQuiz = true;
                    break;

                case R.id.example2:
                    example2TV.setBackgroundColor(Color.rgb(0, 255, 0));
                    userAnswer = 2;
                    isCheckedQuiz = true;
                    break;

                case R.id.example3:
                    example3TV.setBackgroundColor(Color.rgb(0, 255, 0));
                    userAnswer = 3;
                    isCheckedQuiz = true;
                    break;

                default:
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
    }

    void receive()
    {
        while (true)
        {
            try
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1)
                {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();

                Log.d("quizServer", "solveQuiz :" + String.valueOf(data));
                Log.d("quizServer", "4");
                Intent i = new Intent(solveQuiz.this, watchingBroadcasting.class);
                i.putExtra("userId", userId);
                i.putExtra("correctOrNot", data);
                Log.d("quizServer", "solveShow: " + data);
                startActivity(i);
                finish();
            }
            catch (IOException e)
            {
                Log.d("getMsg", e.getMessage() + "");
                try
                {
                    socketChannel.close();
                    break;
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Thread checkUpdate = new Thread()
    {

        public void run()
        {
            try
            {
                Log.d("quizServer", "2");
                String line;
                receive();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };


}
