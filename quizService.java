package com.pedro.rtpstreamer;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class quizService extends Service
{
    SocketChannel socketChannel;
    String data = "test done";
    IBinder binder = new MyBinder();
        private static final String HOST = "192.168.1.7";
//    private static final String HOST = "192.168.10.4";
    private static final int QUIZ_PORT = 5002;
    private boolean correctOrNot;

    public enum getDataPurpose
    {
        QUIZ, SCORE, ANSWER, WINNER
    }

    private int quizNumber, userAnswer;

    @Override
    public void onCreate()
    {
        super.onCreate();

        quizNumber = 1;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d("chk", "service in thread");
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, QUIZ_PORT));
                }
                catch (Exception ioe)
                {
                    ioe.printStackTrace();
                    Log.d("chk", String.valueOf(ioe.getMessage()));
                    Log.d("chk", "service conn ss");
                }
                checkUpdate.start();
            }
        }).start();

    }

    public String getData(final getDataPurpose purpose, int quizNumber, int userAnswer)
    {

        switch (purpose)
        {
            case ANSWER:
                Log.d("chk", "answer1/"+quizNumber+"/"+userAnswer);
                new SendmsgTask().execute("userAnswer/" + quizNumber + "/" + userAnswer);
                break;

            case QUIZ:
                new SendmsgTask().execute("quiz/" + quizNumber);
                break;

            case SCORE:
                new SendmsgTask().execute("score");
                break;

            case WINNER:
                new SendmsgTask().execute("isWinner");
                break;

            default:
                break;
        }
        return data;
    }

    public class MyBinder extends Binder
    {
        public quizService getService()
        {
            return quizService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d("chk", "service onBind");
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public class SendmsgTask extends AsyncTask<String, Void, Void>
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
                Log.d("chk", "inQuizService :" + String.valueOf(data));

                if (data.startsWith("goQuiz"))
                {
                    Intent i = new Intent(this, solveQuiz.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("quizSet", data.substring(6));
                    startActivity(i);
                }
                else if(data.equals("o"))
                {
                    correctOrNot = true;
                }

                else if(data.equals("x"))
                {
                    correctOrNot = false;
                }

                else if(data.equals("score") || data.equals("winner"))
                {
                    Intent i = new Intent(this, correctOrNot.class);
                    i.putExtra("quizResult", correctOrNot);
                    if(data.equals("winner"))
                    {
                        i.putExtra("quizResult", true);
                        i.putExtra("isWinner", true);
                    }
                    startActivity(i);
                }
                else if(data.equals("isWinner"))
                {
                    Log.d("chk", "iswinner 들어가");
                    new SendmsgTask().execute("amIwinner");
                }

//                else if(data.equals("scoreWinner"))
//                {
//                    i.putExtra("isWinner", true);
//                }
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
                Log.d("chk", "2");
                receive();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };
}

