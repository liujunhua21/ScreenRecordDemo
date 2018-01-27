package com.example.liujunhua.screenrecorddemo;

/**
 * Created by liujunhua on 17-12-29.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;



public class RecordService extends Service {
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;

    private boolean running;
    private int width = 480;
    private int height = 960;
    private int dpi;
    public static final int PENDING_REQUEST_CODE = 0x01;
    private static final int NOTIFICATION_ID = 3;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("liujunhua0125","33333");
        startForeground(NOTIFICATION_ID, builder.build());
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //if(DEBUG) Log.d(LOG_TAG,"vCardService onTaskRemoved.");
        Log.d("liujunhua0125","22222");
        super.onTaskRemoved(rootIntent);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        running = false;
        mediaRecorder = new MediaRecorder();
        initNotification();

    }

    private void initNotification() {
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_record)
                //.setContentTitle(getResources().getString(R.string.app_name))
                //.setContentText("正在录制屏幕内容")
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE);
        Intent backIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PENDING_REQUEST_CODE, backIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        RemoteViews rv = new RemoteViews(getPackageName(),R.layout.app_notify_layout);
                 //修改自定义View中的图片(两种方法)
                 //rv.setImageViewResource(R.id.iv,R.mipmap.ic_launcher);
                 //rv.setImageViewBitmap(R.id.iv, BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
                 builder.setContent(rv);

        Intent pauseIntent = new Intent();//intent是一个广播类对象
        pauseIntent.setAction("_notifi_pause");//设置动作
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);//pendingIntent得到广播
        rv.setOnClickPendingIntent(R.id.stop_button,
                pausePendingIntent);//设置按钮成带PendingIntent的可点击按钮


        Intent startIntent = new Intent();
        startIntent.setAction("_notifi_start");
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.start_button,
                startPendingIntent);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d("liujunhua0120","222222222");
        stopForeground(true);
        super.onDestroy();
    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }

        initRecorder();
        createVirtualDisplay();
        mediaRecorder.start();
        running = true;
        startForeground(NOTIFICATION_ID, builder.build());
        Toast.makeText(this, "录制开始", Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
       // mediaRecorder.stop();
        mediaRecorder.reset();
        virtualDisplay.release();
        mediaProjection.stop();
        updateGallery(rootDir);
        stopForeground(true);
        Toast.makeText(this, "录制停止", Toast.LENGTH_SHORT).show();
        return true;
    }


    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(getsaveDirectory() + System.currentTimeMillis() + ".mp4");
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
        try {
            Log.d("liujunhua0125","5555");
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("liujunhua0125","4444");
        }
    }

    public String getsaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }

            Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();
            updateGallery(rootDir);
            return rootDir;
        } else {
            return null;
        }

    }

    public void updateGallery(String filename)//filename是我们的文件全名，包括后缀哦
    {
        MediaScannerConnection.scanFile(this,
                new String[] { filename }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }


    private final IBinder mBinder = new RecordBinder(this);
    public class RecordBinder extends Binder {
        public final RecordService service;


        public RecordBinder(RecordService service) {
            this.service = service;
        }

       public RecordService getRecordService() {
          return service;// RecordService.this;
       }
    }



}