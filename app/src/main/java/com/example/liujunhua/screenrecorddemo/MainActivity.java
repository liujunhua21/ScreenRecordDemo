package com.example.liujunhua.screenrecorddemo;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.os.Binder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
//import android.support.annotation.RequiresApi;


public class MainActivity extends Activity {
    private static final int RECORD_REQUEST_CODE  = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE   = 103;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;
    private final String ACTION_NAME = "发送广播";
    public static final int PENDING_REQUEST_CODE = 0x01;
    private static final int NOTIFICATION_ID = 3;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        setContentView(R.layout.activity_main);

        startBtn =  findViewById(R.id.start_record);
        startBtn.setEnabled(false);
        Log.d("liujunhua333","1111111");
        startBtn.setOnClickListener(new View.OnClickListener() {
           // @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                    if (recordService.isRunning()) {
                        recordService.stopRecord();
                        moveTaskToBack(true);
                        //mNotificationManager.cancel(NOTIFICATION_ID);
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startBtn.setText(R.string.start_record);

                    } else {
                        Intent captureIntent = projectionManager.createScreenCaptureIntent();
                        startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                    }
                }

        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }

        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("_notifi_start");
        intentFilter.addAction("_notifi_pause");
        registerReceiver(new MyRecordReceiver() ,intentFilter);
        //if (recordService.isRunning()) {
            //initNotification();
        //}
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
            moveTaskToBack(true);
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startBtn.setText(R.string.stop_record);
            //moveTaskToBack(true);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //switch(keyCode) {
            //disable the key
            //case KeyEvent.KEYCODE_HOME:
            //case KeyEvent.KEYCODE_BACK:
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            recordService.stopForeground(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
   /* @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }*/

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            startBtn.setEnabled(true);
            startBtn.setText(recordService.isRunning() ? R.string.stop_record : R.string.start_record);
            //((RecordService.RecordBinder) service).service.startService(new Intent(MainActivity.this, RecordService.class));

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };




    public class MyRecordReceiver extends BroadcastReceiver {

        public MyRecordReceiver() {
            Log.d("liujunhua333","33333333");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            context.sendBroadcast(new Intent("screenshot_On"));
            if ("_notifi_start".equals(action)&&!recordService.isRunning()) {
                //在这里做需要做的事情
                //if (recordService.isRunning()) {
                //    recordService.stopRecord();
                //    startBtn.setText(R.string.start_record);
               // } else {
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);

                // }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            if ("_notifi_pause".equals(action)) {
                //在这里做需要做的事情
                //if (recordService.isRunning()) {
                    recordService.stopRecord();
                    startBtn.setText(R.string.start_record);
                    //Toast.makeText(context, "录制停止", Toast.LENGTH_SHORT).show();
               // }
                moveTaskToBack(true);

            }
        }
    }

}