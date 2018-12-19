package com.pedro.rtpstreamer.defaultexample;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtpstreamer.MainActivity;
import com.pedro.rtpstreamer.R;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtpstreamer.chat.adapter;
import com.pedro.rtpstreamer.chat.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.ossrs.rtmp.ConnectCheckerRtmp;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
public class ExampleRtmpActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback
{

    private RtmpCamera1 rtmpCamera1;
    private Button button, goQuiz, quizScore;
    private Boolean quizOrScore = false;
    private Button bRecord;
    private EditText etUrl;
    private static final String BROADCAST_ADDRESS = "rtmp://192.168.1.7/quiz";
//    private static final String BROADCAST_ADDRESS = "rtmp://localhost/quiz";
//    private static final String BROADCAST_ADDRESS = "rtmp://192.168.1.7/quiz";
    private String currentDateAndTime = "", data;
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/rtmp-rtsp-stream-client-java");
    Handler handler;

    private static final String HOST = "192.168.1.7";
    private static final int CHAT_PORT = 5001;
    private static final int QUIZ_PORT = 5002;
    private static final String SUBMIT_QUIZ_ADDRESS = "http://192.168.1.7/getQuiz.php?num=";
    private SocketChannel socketChannel;
    private int quizNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_example);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.b_start_stop);
        button.setOnClickListener(this);
        goQuiz = findViewById(R.id.goQuiz);
        goQuiz.setOnClickListener(this);
        quizScore = findViewById(R.id.quizScore);
        quizScore.setOnClickListener(this);
        bRecord = findViewById(R.id.b_record);
        bRecord.setOnClickListener(this);
        Button switchCamera = findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);
        etUrl = findViewById(R.id.et_rtp_url);
        etUrl.setHint(R.string.hint_rtmp);
        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        surfaceView.getHolder().addCallback(this);
        quizNumber = 1;

        //퀴즈 통신 접
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
                    new SendmsgTask().execute("id:admin");
                }
                catch (Exception ioe)
                {
                    Log.d("asd", ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }
                checkUpdate.start();
            }
        }).start();


    }

    @Override
    public void onConnectionSuccessRtmp()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(ExampleRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(ExampleRtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                        .show();
                rtmpCamera1.stopStream();
                button.setText(R.string.start_button);
            }
        });
    }

    @Override
    public void onDisconnectRtmp()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(ExampleRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(ExampleRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(ExampleRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.b_start_stop:
                if (!rtmpCamera1.isStreaming())
                {
                    if (rtmpCamera1.isRecording() || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo())
                    {
                        button.setText(R.string.stop_button);
//            rtmpCamera1.startStream(etUrl.getText().toString());
                        rtmpCamera1.startStream(BROADCAST_ADDRESS);
                        // rtmpCamera1.startStream("rtmp://13.125.208.211/live");

                    }
                    else
                    {
                        Toast.makeText(this, "Error preparing stream, This device cant do it",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    button.setText(R.string.start_button);
                    rtmpCamera1.stopStream();
                }
                break;

            case R.id.switch_camera:
                try
                {
                    rtmpCamera1.switchCamera();
                }
                catch (CameraOpenException e)
                {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.b_record:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    if (!rtmpCamera1.isRecording())
                    {
                        try
                        {
                            if (!folder.exists())
                            {
                                folder.mkdir();
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                            currentDateAndTime = sdf.format(new Date());
                            if (!rtmpCamera1.isStreaming())
                            {
                                if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo())
                                {
                                    rtmpCamera1.startRecord(
                                            folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                    bRecord.setText(R.string.stop_record);
                                    Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(this, "Error preparing stream, This device cant do it",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                rtmpCamera1.startRecord(
                                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                bRecord.setText(R.string.stop_record);
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (IOException e)
                        {
                            rtmpCamera1.stopRecord();
                            bRecord.setText(R.string.start_record);
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        rtmpCamera1.stopRecord();
                        bRecord.setText(R.string.start_record);
                        Toast.makeText(this,
                                "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                        currentDateAndTime = "";
                    }
                }
                else
                {
                    Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.goQuiz:
                Log.d("php", "o");

                //    퀴즈내고 사용자에게 전달하기 위한 네티 통신
                new SendmsgTask().execute("quiz/"+quizNumber);
//                GetData task = new GetData();
//                task.execute(SUBMIT_QUIZ_ADDRESS+quizNumber, "");
                quizNumber++;

                if(quizNumber == 4)
                {
                    goQuiz.setText("퀴즈 마감");
                    goQuiz.setBackgroundColor(Color.rgb(255, 0,0));
                }

                break;

            case R.id.quizScore:
                quizOrScore = true;
                new SendmsgTask().execute("score");
                break;

            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
    {
        rtmpCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording())
        {
            rtmpCamera1.stopRecord();
            bRecord.setText(R.string.start_record);
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
        }
        if (rtmpCamera1.isStreaming())
        {
            rtmpCamera1.stopStream();
            button.setText(getResources().getString(R.string.start_button));
        }
        rtmpCamera1.stopPreview();
    }

//    php 에서 값 가져옴. 라라벨로 변경 필요
    private class GetData extends AsyncTask<String, Void, String>
    {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ExampleRtmpActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d("php", "response - " + result);

            if (result == null)
            {
                Log.d("php", "result = null");
            }
            else
            {
                String[] quizSet = result.split("\\|");
                String[] examples = quizSet[2].split("/");

                Log.d("php", "문항: " + quizSet[0]);
                Log.d("php", "문제: " + quizSet[1]);
                Log.d("php", "보기1: " + examples[0]);
                Log.d("php", "보기2: " + examples[1]);
                Log.d("php", "보기3: " + examples[2]);
//                Log.d("php", "정답: " + quizSet[3]);

                new SendmsgTask().execute("admin: "+result);
//                new SendmsgTask().execute("asd");
            }
        }

        @Override
        protected String doInBackground(String... params)
        {
            String serverURL = params[0];
            String postParameters = params[1];

            try
            {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("php", "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK)
                {
                    inputStream = httpURLConnection.getInputStream();
                }
                else
                {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            }
            catch (Exception e)
            {

                Log.d("php", "GetData : Error ", e);
                errorString = e.toString();

                return null;
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
                Log.d("receive", "msg :" + data);
                handler.post(showUpdate);
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
                String line;
                receive();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private Runnable showUpdate = new Runnable()
    {

        public void run()
        {
            String receive = "Coming word : " + data;
            Log.d("getChat", receive);

//            chatRecycler = findViewById(R.id.chatRecyclerView);
//            //chatRecycler.setHasFixedSize(true);
//            chatLayoutManager = new LinearLayoutManager(getApplicationContext());
//            //((LinearLayoutManager) chatLayoutManager).setReverseLayout(true);
//            ((LinearLayoutManager) chatLayoutManager).setStackFromEnd(true);
//            chatRecycler.setLayoutManager(chatLayoutManager);
//            adapter chatAdapter = new adapter(chatArrayList);
//            chatRecycler.setAdapter(chatAdapter);
//
//            if(chatArrayList.size() == 0)
//            {
//                chatArrayList.add(0, new chat("ann", data));
//            }
//            else
//            {
//                chatArrayList.add(chatArrayList.size(), new chat("ann", data));
//            }
        }

    };
}