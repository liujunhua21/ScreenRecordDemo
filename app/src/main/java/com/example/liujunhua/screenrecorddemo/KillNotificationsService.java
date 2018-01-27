package com.example.liujunhua.screenrecorddemo;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;


/**
 * Created by liujunhua on 18-1-25.
 */

public class KillNotificationsService extends Service{

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();


    }
}
