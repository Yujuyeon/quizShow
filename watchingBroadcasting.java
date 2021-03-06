package com.pedro.rtpstreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.chat.adapter;
import com.pedro.rtpstreamer.chat.chat;
import com.pedro.rtpstreamer.databinding.ActivityWatchingBroadcastingBinding;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import android.databinding.DataBindingUtil;
import android.widget.Toast;

public class watchingBroadcasting extends AppCompatActivity
{
    //퀴즈 서비스 변수 모
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

    //    private static final String userId = "TEST";
    private static final String QUIZADDRESS = "rtmp://192.168.1.7/quiz";
    RecyclerView chatRecycler;
    RecyclerView.LayoutManager chatLayoutManager;

    //채팅
    private Handler handler;
    private String chatData, quizData, userId, correctOrNot;
    SocketChannel chatChannel, quizChannel;
    private static final String HOST = "192.168.1.7";
    private static final int CHAT_PORT = 5001;
    private static final int QUIZ_PORT = 5002;
    ActivityWatchingBroadcastingBinding binding;
    ArrayList<chat> chatArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watching_broadcasting);

        Intent intent = new Intent(this, quizService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Intent i = getIntent();
        userId = i.getStringExtra("userId");
        correctOrNot = i.getStringExtra("correctOrNot");

        Log.d("chk", "유저 아이디: " + userId);
        //채팅 통신
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watching_broadcasting);
        handler = new Handler();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
//                while(true)
//                {
//                    Log.d("chk", quizService.data);
//
//                }
            }
        }).start();
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    chatChannel = SocketChannel.open();
                    chatChannel.configureBlocking(true);
                    chatChannel.connect(new InetSocketAddress(HOST, CHAT_PORT));
                    new SendmsgTask().execute("id:" + userId);
                }
                catch (Exception ioe)
                {
                    Log.d("asd", ioe.getMessage() + "1");
                    ioe.printStackTrace();
                }
                chatCheckUpdate.start();
            }
        }).start();

//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    Log.d("asd", "oo");
//                    quizChannel = SocketChannel.open();
//                    quizChannel.configureBlocking(true);
//                    quizChannel.connect(new InetSocketAddress(HOST, QUIZ_PORT));
//                    new SendQuizTask().execute("id:" + userId);
////                        quizChannel.close();
//                }
//                catch (Exception ioe)
//                {
//                    Log.d("asd", ioe.getMessage() + "2");
//                    ioe.printStackTrace();
//
//                }
//                quizCheckUpdate.start();
//            }
//        }).start();

        binding.sendMsgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    final String return_msg = binding.sendMsgEditText.getText().toString();
                    if (!TextUtils.isEmpty(return_msg))
                    {
                        chatRecycler = findViewById(R.id.chatRecyclerView);
                        chatRecycler.setHasFixedSize(true);
                        chatLayoutManager = new LinearLayoutManager(getApplicationContext());
                        chatRecycler.setLayoutManager(chatLayoutManager);
                        adapter chatAdapter = new adapter(chatArrayList);
                        chatRecycler.setAdapter(chatAdapter);
                        ((LinearLayoutManager) chatLayoutManager).setStackFromEnd(true);

                        if (chatArrayList.size() == 0)
                        {
                            chatArrayList.add(0, new chat(userId, return_msg));
                        }
                        else
                        {
                            chatArrayList.add(chatArrayList.size(), new chat(userId, return_msg));
                        }
                        new SendmsgTask().execute(userId + "/" + return_msg);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.d("asd", e.getMessage() + "3");
                }
            }
        });


        //플레이어
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        PlayerView playerView = findViewById(R.id.exoplayer);
        playerView.setPlayer(player);

        /*
          Create RTMP Data Source
         */

        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();

//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_750.mp4"),
//                rtmpDataSourceFactory, extractorsFactory, null, null);

        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                // .createMediaSource(Uri.parse("rtmp://stream1.livestreamingservices.com:1935/tvmlive/tvmlive"));
                .createMediaSource(Uri.parse(QUIZADDRESS));
        player.prepare(videoSource);
        player.setPlayWhenReady(true);

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unbindService(serviceConnection);
        try
        {
            quizChannel.close();
            chatChannel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //서버로 채팅 전달
    private class SendmsgTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings)
        {
            try
            {
                chatChannel
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
                    binding.sendMsgEditText.setText("");
                }
            });
        }
    }

    private class SendQuizTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings)
        {
            try
            {
                quizChannel
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
                ByteBuffer chatByteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int chatReadByteCount = chatChannel.read(chatByteBuffer); //데이터받기
                Log.d("readByteCount", chatReadByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (chatReadByteCount == -1)
                {
                    throw new IOException();
                }
                chatByteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                chatData = charset.decode(chatByteBuffer).toString();
                Log.d("receive", "msg :" + chatData);

                handler.post(showUpdate);
            }
            catch (IOException e)
            {
                Log.d("getMsg", e.getMessage() + "");
                try
                {
                    chatChannel.close();
                    break;
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }
            }
        }
    }

    void receiveQuiz()
    {
        while (true)
        {
            try
            {
                ByteBuffer quizByteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int quizReadByteCount = quizChannel.read(quizByteBuffer); //데이터받기
                Log.d("readByteCount", quizReadByteCount + "");

                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (quizReadByteCount == -1)
                {
                    throw new IOException();
                }
                quizByteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                quizData = charset.decode(quizByteBuffer).toString();

                Log.d("quizServer", "watchShow :" + quizData);
//                Log.d("quizServer", quizData);
//                퀴즈는 내는 경우
                if (quizData.startsWith("goQuiz"))
                {

//                퀴즈 데이터는 문항번호|퀴즈|보기1/2/3 형태로 전달 받음
                    Intent i = new Intent(watchingBroadcasting.this, solveQuiz.class);
                    i.putExtra("quizSet", quizData.substring(6));
                    i.putExtra("userId", userId);
                    quizChannel.close();
                    startActivity(i);
//                    finish();
                }
//                채점하는 경우
                else if (quizData.equals("score"))
                {
                    Log.d("quizServer", "채점");
                    Intent i = new Intent(this, correctOrNot.class);
                    i.putExtra("quizResult", correctOrNot);
                    startActivity(i);
                }
            }
            catch (IOException e)
            {
                Log.d("getMsg", e.getMessage() + "");
                try
                {
                    chatChannel.close();
                    break;
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Thread chatCheckUpdate = new Thread()
    {

        public void run()
        {
            try
            {
                receive();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private Thread quizCheckUpdate = new Thread()
    {

        public void run()
        {
            try
            {
                receiveQuiz();
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
            String receive = "Coming word : " + chatData;
            Log.d("getChat", receive);

            chatRecycler = findViewById(R.id.chatRecyclerView);
            //chatRecycler.setHasFixedSize(true);
            chatLayoutManager = new LinearLayoutManager(getApplicationContext());
            //((LinearLayoutManager) chatLayoutManager).setReverseLayout(true);
            ((LinearLayoutManager) chatLayoutManager).setStackFromEnd(true);
            chatRecycler.setLayoutManager(chatLayoutManager);
            adapter chatAdapter = new adapter(chatArrayList);
            chatRecycler.setAdapter(chatAdapter);

            if (chatArrayList.size() == 0)
            {
                chatArrayList.add(0, new chat(chatData.split("/")[0], chatData.split("/")[1]));
            }
            else
            {
                chatArrayList.add(chatArrayList.size(), new chat(chatData.split("/")[0], chatData.split("/")[1]));
            }
        }

    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            chatChannel.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getLocalServerIp()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
        }
        return null;
    }
    @Override
    public void onBackPressed() {

        // Alert을 이용해 종료시키기
        AlertDialog.Builder dialog = new AlertDialog.Builder(watchingBroadcasting.this);
        dialog  .setTitle("종료 알림")
                .setMessage("정말 종료하시겠습니까?")
                .setPositiveButton("종료합니다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("취소합니다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(watchingBroadcasting.this, "취소했습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(watchingBroadcasting.this, "종료하지 않습니다", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }



}